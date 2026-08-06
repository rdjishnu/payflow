package com.payflow.payflow.Service;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.payflow.payflow.config.RabbitConfig;
import com.payflow.payflow.model.OrderStatus;
import com.payflow.payflow.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void processPayment(String orderId) {
        orderRepository.findById(UUID.fromString(orderId)).ifPresent(order -> {
            // simulate a flaky payment gateway: ~70% success
            boolean success = Math.random() > 0.3;
            order.setStatus(success ? OrderStatus.PAID : OrderStatus.FAILED);
            orderRepository.save(order);
            System.out.println("Processed payment for order " + orderId + " -> " + order.getStatus());
        });
    }
}