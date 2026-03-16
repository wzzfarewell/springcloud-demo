package com.demo.order.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 事件发布调度器。
 *
 * <p>
 * 每 3 秒轮询一次 PENDING 事件并发布到 RabbitMQ：
 * <ul>
 * <li>成功发布 → 标记为 PUBLISHED</li>
 * <li>发布失败 → 标记为 FAILED（可配合告警或重试策略）</li>
 * </ul>
 *
 * <p>
 * 幂等保障：{@link OutboxEvent#version} 乐观锁 + 状态流转，
 * 多实例并发时只有一个实例能成功更新状态，其余因版本冲突跳过。
 *
 * <p>
 * 至少一次语义（At-least-once）：RabbitMQ 消费者需保证幂等处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	@Scheduled(fixedDelay = 3000)
	@Transactional
	public void publishPendingEvents() {
		List<OutboxEvent> pending = outboxEventRepository
				.findTop20ByStatusOrderByCreatedAtAsc("PENDING");

		if (pending.isEmpty()) {
			return;
		}

		log.debug("Processing {} pending outbox events", pending.size());

		for (OutboxEvent event : pending) {
			try {
				// 反序列化为原始类型，RabbitMQ Jackson 转换器会保留 __TypeId__ 头
				Class<?> payloadClass = Class.forName(event.getPayloadType());
				Object payload = objectMapper.readValue(event.getPayload(), payloadClass);

				rabbitTemplate.convertAndSend(event.getExchange(), event.getRoutingKey(), payload);

				event.setStatus("PUBLISHED");
				event.setPublishedAt(LocalDateTime.now());
				log.info("Outbox published: id={}, routingKey={}", event.getId(), event.getRoutingKey());

			} catch (Exception e) {
				event.setStatus("FAILED");
				log.error("Outbox publish failed: id={}, error={}", event.getId(), e.getMessage());
			}
		}
	}
}
