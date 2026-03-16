package com.demo.common.constant;

public interface RabbitMQConstants {
	// User events
	String USER_EXCHANGE = "user.exchange";
	String USER_CREATED_QUEUE = "user.created.queue";
	String USER_CREATED_ROUTING_KEY = "user.created";

	// Order events
	String ORDER_EXCHANGE = "order.exchange";
	String ORDER_CREATED_QUEUE = "order.created.queue";
	String ORDER_CREATED_ROUTING_KEY = "order.created";
	String ORDER_PAID_QUEUE = "order.paid.queue";
	String ORDER_PAID_ROUTING_KEY = "order.paid";

	// Dead letter
	String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
	String DEAD_LETTER_QUEUE = "dead.letter.queue";
}
