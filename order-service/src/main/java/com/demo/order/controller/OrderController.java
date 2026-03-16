package com.demo.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.demo.common.response.Result;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.demo.common.dto.OrderDTO;
import com.demo.order.dto.CreateOrderRequest;
import com.demo.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Result<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		return Result.success(orderService.createOrder(request));
	}

	@GetMapping("/{id}")
	public Result<OrderDTO> getOrderById(@PathVariable Long id) {
		return Result.success(orderService.getOrderById(id));
	}

	@GetMapping("/user/{userId}")
	public Result<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
		return Result.success(orderService.getOrdersByUserId(userId));
	}

	@GetMapping
	public Result<List<OrderDTO>> getAllOrders() {
		return Result.success(orderService.getAllOrders());
	}

	@PutMapping("/{id}/status")
	public Result<OrderDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
		return Result.success(orderService.updateOrderStatus(id, status));
	}

	@PutMapping("/{id}/cancel")
	public Result<Void> cancelOrder(@PathVariable Long id) {
		orderService.cancelOrder(id);
		return Result.success();
	}
}
