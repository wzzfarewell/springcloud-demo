package com.demo.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
	private Long id;
	private Long userId;
	private String orderNo;
	private String status;
	private BigDecimal totalAmount;
	private String remark;
	private List<OrderItemDTO> items;
	private LocalDateTime createdAt;
}
