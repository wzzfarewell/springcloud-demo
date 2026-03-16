package com.demo.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.demo.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByCategory(String category);

	List<Product> findByAvailableTrue();

	List<Product> findByNameContainingIgnoreCase(String keyword);

	@Modifying
	@Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
	int deductStock(Long id, Integer quantity);
}
