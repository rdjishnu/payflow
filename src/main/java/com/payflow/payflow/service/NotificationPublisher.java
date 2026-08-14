package com.payflow.payflow.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.payflow.payflow.config.RabbitConfig;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(String orderId, String status) {
        String message = String.format("Order %s status updated to %s", orderId, status);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.NOTIFICATION_ROUTING_KEY, message);
    }
}