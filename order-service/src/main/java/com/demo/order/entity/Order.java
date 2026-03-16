package com.demo.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 32)
	private String orderNo;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal totalAmount;

	/** PENDING / PAID / SHIPPED / COMPLETED / CANCELLED */
	@Column(nullable = false, length = 20)
	@Builder.Default
	private String status = "PENDING";

	@Column(length = 500)
	private String remark;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	@ToString.Exclude
	private List<OrderItem> items = new ArrayList<>();

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
