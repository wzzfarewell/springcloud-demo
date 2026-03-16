package com.demo.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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

	@Bean
	public DirectExchange userExchange() {
		return new DirectExchange(RabbitMQConstants.USER_EXCHANGE, true, false);
	}

	@Bean
	public Queue userCreatedQueue() {
		return new Queue(RabbitMQConstants.USER_CREATED_QUEUE, true);
	}

	@Bean
	public Binding userCreatedBinding(Queue userCreatedQueue, DirectExchange userExchange) {
		return BindingBuilder.bind(userCreatedQueue)
				.to(userExchange)
				.with(RabbitMQConstants.USER_CREATED_ROUTING_KEY);
	}

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
