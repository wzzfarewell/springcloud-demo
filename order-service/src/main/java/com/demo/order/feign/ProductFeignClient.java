package com.demo.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.demo.common.response.Result;

import com.demo.common.dto.ProductDTO;

/**
 * 通过 OpenFeign + Eureka 服务发现调用 product-service
 */
@FeignClient(name = "product-service")
public interface ProductFeignClient {

	@GetMapping("/api/products/{id}")
	Result<ProductDTO> getProductById(@PathVariable("id") Long id);

	@PutMapping("/api/products/{id}/stock")
	Result<Void> deductStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);
}
