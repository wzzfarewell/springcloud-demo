package com.demo.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.demo.common.constant.RabbitMQConstants;

@Configuration
public class RabbitMQConfig {

	// ---- Order Exchange & Queues ----

	@Bean
	public DirectExchange orderExchange() {
		return new DirectExchange(RabbitMQConstants.ORDER_EXCHANGE, true, false);
	}

	@Bean
	public Queue orderCreatedQueue() {
		return new Queue(RabbitMQConstants.ORDER_CREATED_QUEUE, true);
	}

	@Bean
	public Queue orderPaidQueue() {
		return new Queue(RabbitMQConstants.ORDER_PAID_QUEUE, true);
	}

	@Bean
	public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange) {
		return BindingBuilder.bind(orderCreatedQueue)
				.to(orderExchange)
				.with(RabbitMQConstants.ORDER_CREATED_ROUTING_KEY);
	}

	@Bean
	public Binding orderPaidBinding(Queue orderPaidQueue, DirectExchange orderExchange) {
		return BindingBuilder.bind(orderPaidQueue)
				.to(orderExchange)
				.with(RabbitMQConstants.ORDER_PAID_ROUTING_KEY);
	}

	// ---- Dead Letter ----

	@Bean
	public DirectExchange deadLetterExchange() {
		return new DirectExchange(RabbitMQConstants.DEAD_LETTER_EXCHANGE, true, false);
	}

	@Bean
	public Queue deadLetterQueue() {
		return new Queue(RabbitMQConstants.DEAD_LETTER_QUEUE, true);
	}

	// ---- Message Converter & Template ----

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return new Jackson2JsonMessageConverter(mapper);
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);
		return template;
	}
}
