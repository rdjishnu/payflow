package com.payflow.payflow.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.payflow.payflow.config.RabbitConfig;

@Service
public class NotificationConsumer {

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleNotification(String message) {
        System.out.println("NOTIFICATION SENT: " + message);
    }
}