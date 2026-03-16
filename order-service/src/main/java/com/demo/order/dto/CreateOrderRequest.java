package com.demo.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
	@NotNull(message = "用户 ID 不能为空")
	private Long userId;

	@NotEmpty(message = "订单项不能为空")
	@Valid
	private List<OrderItemRequest> items;

	private String remark;
}
