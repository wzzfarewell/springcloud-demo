package com.demo.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class ProductDTO {
	private Long id;
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stock;
	private String category;
	private LocalDateTime createdAt;
}
