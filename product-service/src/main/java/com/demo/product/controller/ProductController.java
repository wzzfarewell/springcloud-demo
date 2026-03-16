package com.demo.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.demo.common.response.Result;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.demo.common.dto.ProductDTO;
import com.demo.product.dto.CreateProductRequest;
import com.demo.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Result<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
		return Result.success(productService.createProduct(request));
	}

	@GetMapping("/{id}")
	public Result<ProductDTO> getProductById(@PathVariable Long id) {
		return Result.success(productService.getProductById(id));
	}

	@GetMapping
	public Result<List<ProductDTO>> getAllProducts() {
		return Result.success(productService.getAllProducts());
	}

	@GetMapping("/category/{category}")
	public Result<List<ProductDTO>> getByCategory(@PathVariable String category) {
		return Result.success(productService.getProductsByCategory(category));
	}

	@GetMapping("/search")
	public Result<List<ProductDTO>> search(@RequestParam String keyword) {
		return Result.success(productService.searchProducts(keyword));
	}

	@PutMapping("/{id}")
	public Result<ProductDTO> updateProduct(@PathVariable Long id,
			@Valid @RequestBody CreateProductRequest request) {
		return Result.success(productService.updateProduct(id, request));
	}

	@PutMapping("/{id}/stock")
	public Result<Void> deductStock(@PathVariable Long id,
			@RequestParam Integer quantity) {
		productService.deductStock(id, quantity);
		return Result.success();
	}

	@DeleteMapping("/{id}")
	public Result<Void> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return Result.success();
	}
}
