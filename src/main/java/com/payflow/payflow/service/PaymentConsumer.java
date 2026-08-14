package com.payflow.payflow.service;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.payflow.payflow.config.RabbitConfig;
import com.payflow.payflow.model.OrderStatus;
import com.payflow.payflow.repository.OrderRepository;

@Service
public class PaymentConsumer {

    private final OrderRepository orderRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationPublisher notificationPublisher;

    public PaymentConsumer(OrderRepository orderRepository, PaymentGatewayService paymentGatewayService, NotificationPublisher notificationPublisher) {
        this.orderRepository = orderRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.notificationPublisher = notificationPublisher;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void processPayment(String orderId) {
        orderRepository.findById(UUID.fromString(orderId)).ifPresent(order -> {
            boolean success = paymentGatewayService.charge(orderId);

            if (success) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                notificationPublisher.sendNotification(orderId, "PAID");
            } else {
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
                notificationPublisher.sendNotification(orderId, "FAILED");
                compensate();
            }
        });
    }

    private void compensate() {
    }
}