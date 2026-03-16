package com.demo.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {
	@NotBlank(message = "商品名称不能为空")
	private String name;

	private String description;

	@NotNull(message = "价格不能为空")
	@DecimalMin(value = "0.01", message = "价格必须大于 0")
	private BigDecimal price;

	@Min(value = 0, message = "库存不能为负数")
	private Integer stock = 0;

	private String category;
}
