package com.demo.order.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import com.demo.common.constant.RabbitMQConstants;
import com.demo.common.dto.OrderDTO;

/**
 * 订单消息消费者，监听订单相关事件。
 * 实际场景中可用于：推送通知、积分累加、风控审计等后续处理。
 */
@Slf4j
@Component
public class OrderMessageConsumer {

	@RabbitListener(queues = RabbitMQConstants.ORDER_CREATED_QUEUE)
	public void onOrderCreated(OrderDTO order) {
		log.info("[MQ] Received order.created - orderNo={}, userId={}, amount={}",
				order.getOrderNo(), order.getUserId(), order.getTotalAmount());
		// TODO: 可在此处触发推送通知、库存预警等业务逻辑
	}

	@RabbitListener(queues = RabbitMQConstants.ORDER_PAID_QUEUE)
	public void onOrderPaid(OrderDTO order) {
		log.info("[MQ] Received order.paid - orderNo={}, userId={}, amount={}",
				order.getOrderNo(), order.getUserId(), order.getTotalAmount());
		// TODO: 可在此处触发发货流程、积分累加等业务逻辑
	}
}
