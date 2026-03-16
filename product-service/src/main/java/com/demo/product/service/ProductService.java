package com.demo.product.service;

import java.util.List;

import com.demo.product.dto.CreateProductRequest;

import com.demo.common.dto.ProductDTO;

public interface ProductService {
	ProductDTO createProduct(CreateProductRequest request);

	ProductDTO getProductById(Long id);

	List<ProductDTO> getAllProducts();

	List<ProductDTO> getProductsByCategory(String category);

	List<ProductDTO> searchProducts(String keyword);

	ProductDTO updateProduct(Long id, CreateProductRequest request);

	void deductStock(Long id, Integer quantity);

	void deleteProduct(Long id);
}
