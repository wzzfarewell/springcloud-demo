package com.demo.product.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.common.constant.RedisKeyConstants;
import com.demo.common.dto.ProductDTO;
import com.demo.common.exception.BusinessException;
import com.demo.product.dto.CreateProductRequest;
import com.demo.product.entity.Product;
import com.demo.product.lock.DistributedLock;
import com.demo.product.repository.ProductRepository;
import com.demo.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	@Transactional
	public ProductDTO createProduct(CreateProductRequest request) {
		Product product = Product.builder()
				.name(request.getName())
				.description(request.getDescription())
				.price(request.getPrice())
				.stock(request.getStock())
				.category(request.getCategory())
				.build();
		return toDTO(productRepository.save(product));
	}

	@Override
	public ProductDTO getProductById(Long id) {
		String cacheKey = RedisKeyConstants.PRODUCT_KEY_PREFIX + id;
		ProductDTO cached = (ProductDTO) redisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			log.debug("Cache hit for product id: {}", id);
			return cached;
		}
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new BusinessException(2001, "商品不存在"));
		ProductDTO dto = toDTO(product);
		redisTemplate.opsForValue().set(cacheKey, dto, RedisKeyConstants.PRODUCT_CACHE_TTL, TimeUnit.SECONDS);
		return dto;
	}

	@Override
	public List<ProductDTO> getAllProducts() {
		return productRepository.findByAvailableTrue().stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> getProductsByCategory(String category) {
		return productRepository.findByCategory(category).stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> searchProducts(String keyword) {
		return productRepository.findByNameContainingIgnoreCase(keyword).stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ProductDTO updateProduct(Long id, CreateProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new BusinessException(2001, "商品不存在"));
		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setStock(request.getStock());
		product.setCategory(request.getCategory());
		product = productRepository.save(product);
		// 清除 Redis 缓存
		redisTemplate.delete(RedisKeyConstants.PRODUCT_KEY_PREFIX + id);
		return toDTO(product);
	}

	@Override
	@Transactional
	@DistributedLock(key = "'stock:' + #id", leaseTime = 5000, waitTime = 3000)
	public void deductStock(Long id, Integer quantity) {
		int updated = productRepository.deductStock(id, quantity);
		if (updated == 0) {
			throw new BusinessException(2002, "库存不足或商品不存在");
		}
		// 清除 Redis 缓存，确保数据一致性
		redisTemplate.delete(RedisKeyConstants.PRODUCT_KEY_PREFIX + id);
	}

	@Override
	@Transactional
	public void deleteProduct(Long id) {
		if (!productRepository.existsById(id)) {
			throw new BusinessException(2001, "商品不存在");
		}
		productRepository.deleteById(id);
		redisTemplate.delete(RedisKeyConstants.PRODUCT_KEY_PREFIX + id);
	}

	private ProductDTO toDTO(Product product) {
		return ProductDTO.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice())
				.stock(product.getStock())
				.category(product.getCategory())
				.createdAt(product.getCreatedAt())
				.build();
	}
}
