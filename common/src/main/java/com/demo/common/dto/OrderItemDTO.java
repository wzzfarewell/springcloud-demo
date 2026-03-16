package com.demo.common.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
	private Long id;
	private Long productId;
	private String productName;
	private Integer quantity;
	private BigDecimal price;
	private BigDecimal subtotal;
}
