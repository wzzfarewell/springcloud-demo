package com.demo.order.outbox;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transactional Outbox 事件记录。
 *
 * <p>
 * 核心思路：将"要发送的消息"与业务数据写入同一本地事务，
 * 由调度器异步轮询并发布到 RabbitMQ，解决以下分布式事务问题：
 *
 * <pre>
 * 问题场景：
 *   Step 1: DB commit -> 订单保存成功 ✓
 *   Step 2: rabbitTemplate.send -> 应用崩溃 → 消息永久丢失 ✗
 *
 * Outbox 解决方案：
 *   Step 1: DB commit -> 订单 + OutboxEvent 同时写入 DB ✓（原子）
 *   Step 2: @Scheduled 轮询 PENDING 事件 -> 发布到 RabbitMQ ✓
 *   Step 3: 发布成功 -> 标记 PUBLISHED ✓（幂等保证）
 * </pre>
 *
 * <p>
 * {@link #version} 乐观锁，防止多实例并发重复消费同一事件。
 */
@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** RabbitMQ Exchange 名称，例如 order.exchange */
	@Column(nullable = false, length = 64)
	private String exchange;

	/** RabbitMQ Routing Key，例如 order.created */
	@Column(nullable = false, length = 64)
	private String routingKey;

	/** 消息 payload 的 Java 类全限定名，用于反序列化时恢复类型 */
	@Column(nullable = false, length = 256)
	private String payloadType;

	/** 序列化为 JSON 的消息内容 */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	/** 事件状态：PENDING → PUBLISHED | FAILED */
	@Column(nullable = false, length = 20)
	@Builder.Default
	private String status = "PENDING";

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime publishedAt;

	/** 乐观锁版本号，防止多实例并发重复处理 */
	@Version
	private Long version;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
