package com.demo.order.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	/**
	 * 按创建时间升序取最早的 PENDING 事件（批量处理，防止积压）。
	 */
	List<OutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(String status);
}
