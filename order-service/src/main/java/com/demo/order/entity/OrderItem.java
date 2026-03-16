package com.demo.order.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	@ToString.Exclude
	private Order order;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false, length = 100)
	private String productName;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal price;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal subtotal;
}
