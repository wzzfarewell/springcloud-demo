package com.demo.order.service;

import java.util.List;

import com.demo.common.dto.OrderDTO;
import com.demo.order.dto.CreateOrderRequest;

public interface OrderService {
	OrderDTO createOrder(CreateOrderRequest request);

	OrderDTO getOrderById(Long id);

	List<OrderDTO> getOrdersByUserId(Long userId);

	List<OrderDTO> getAllOrders();

	OrderDTO updateOrderStatus(Long id, String status);

	void cancelOrder(Long id);
}
