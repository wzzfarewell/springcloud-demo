package com.demo.order.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.common.exception.BusinessException;
import com.demo.common.response.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.demo.common.constant.RabbitMQConstants;
import com.demo.common.dto.OrderDTO;
import com.demo.common.dto.OrderItemDTO;
import com.demo.common.dto.ProductDTO;
import com.demo.common.dto.UserDTO;
import com.demo.order.dto.CreateOrderRequest;
import com.demo.order.dto.OrderItemRequest;
import com.demo.order.entity.Order;
import com.demo.order.entity.OrderItem;
import com.demo.order.feign.ProductFeignClient;
import com.demo.order.feign.UserFeignClient;
import com.demo.order.repository.OrderRepository;
import com.demo.order.service.OrderService;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;
	private final UserFeignClient userFeignClient;
	private final ProductFeignClient productFeignClient;
	private final RabbitTemplate rabbitTemplate;

	@Override
	@Transactional
	public OrderDTO createOrder(CreateOrderRequest request) {
		// 1. 通过 OpenFeign 校验用户是否存在
		Result<UserDTO> userResult = userFeignClient.getUserById(request.getUserId());
		if (userResult == null || userResult.getData() == null) {
			throw new BusinessException(1001, "用户不存在");
		}

		// 2. 查询商品信息并计算金额
		List<OrderItem> items = new ArrayList<>();
		BigDecimal totalAmount = BigDecimal.ZERO;

		for (OrderItemRequest itemReq : request.getItems()) {
			Result<ProductDTO> productResult = productFeignClient.getProductById(itemReq.getProductId());
			if (productResult == null || productResult.getData() == null) {
				throw new BusinessException(2001, "商品不存在: " + itemReq.getProductId());
			}
			ProductDTO product = productResult.getData();
			if (product.getStock() < itemReq.getQuantity()) {
				throw new BusinessException(2002, "商品库存不足: " + product.getName());
			}

			BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
			totalAmount = totalAmount.add(subtotal);

			items.add(OrderItem.builder()
					.productId(product.getId())
					.productName(product.getName())
					.quantity(itemReq.getQuantity())
					.price(product.getPrice())
					.subtotal(subtotal)
					.build());
		}

		// 3. 创建订单
		Order order = Order.builder()
				.orderNo(generateOrderNo())
				.userId(request.getUserId())
				.totalAmount(totalAmount)
				.status("PENDING")
				.remark(request.getRemark())
				.build();

		items.forEach(item -> item.setOrder(order));
		order.setItems(items);

		Order saved = orderRepository.save(order);

		// 4. 扣减库存（远程调用 product-service）
		for (OrderItemRequest itemReq : request.getItems()) {
			productFeignClient.deductStock(itemReq.getProductId(), itemReq.getQuantity());
		}

		OrderDTO orderDTO = toDTO(saved);

		// 5. 发布订单创建事件到 RabbitMQ
		try {
			rabbitTemplate.convertAndSend(
					RabbitMQConstants.ORDER_EXCHANGE,
					RabbitMQConstants.ORDER_CREATED_ROUTING_KEY,
					orderDTO);
			log.info("Published order.created event, orderNo={}", saved.getOrderNo());
		} catch (Exception e) {
			log.warn("Failed to publish order.created event: {}", e.getMessage());
		}

		return orderDTO;
	}

	@Override
	public OrderDTO getOrderById(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(3001, "订单不存在"));
		return toDTO(order);
	}

	@Override
	public List<OrderDTO> getOrdersByUserId(Long userId) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<OrderDTO> getAllOrders() {
		return orderRepository.findAll().stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public OrderDTO updateOrderStatus(Long id, String status) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(3001, "订单不存在"));
		order.setStatus(status);
		order = orderRepository.save(order);

		// 订单支付完成时发布 order.paid 事件
		if ("PAID".equals(status)) {
			try {
				rabbitTemplate.convertAndSend(
						RabbitMQConstants.ORDER_EXCHANGE,
						RabbitMQConstants.ORDER_PAID_ROUTING_KEY,
						toDTO(order));
				log.info("Published order.paid event, orderNo={}", order.getOrderNo());
			} catch (Exception e) {
				log.warn("Failed to publish order.paid event: {}", e.getMessage());
			}
		}
		return toDTO(order);
	}

	@Override
	@Transactional
	public void cancelOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(3001, "订单不存在"));
		if ("PAID".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus())) {
			throw new BusinessException(3002, "已支付或已发货的订单不能取消");
		}
		order.setStatus("CANCELLED");
		orderRepository.save(order);
	}

	private String generateOrderNo() {
		String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
		return "ORD" + timeStr + suffix;
	}

	private OrderDTO toDTO(Order order) {
		List<OrderItemDTO> itemDTOs = order.getItems().stream()
				.map(item -> OrderItemDTO.builder()
						.id(item.getId())
						.productId(item.getProductId())
						.productName(item.getProductName())
						.quantity(item.getQuantity())
						.price(item.getPrice())
						.subtotal(item.getSubtotal())
						.build())
				.collect(Collectors.toList());

		return OrderDTO.builder()
				.id(order.getId())
				.userId(order.getUserId())
				.orderNo(order.getOrderNo())
				.status(order.getStatus())
				.totalAmount(order.getTotalAmount())
				.remark(order.getRemark())
				.items(itemDTOs)
				.createdAt(order.getCreatedAt())
				.build();
	}
}
