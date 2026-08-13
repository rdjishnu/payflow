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

    // Manual constructor replacing @RequiredArgsConstructor
    public PaymentConsumer(OrderRepository orderRepository, PaymentGatewayService paymentGatewayService) {
        this.orderRepository = orderRepository;
        this.paymentGatewayService = paymentGatewayService;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void processPayment(String orderId) {
        orderRepository.findById(UUID.fromString(orderId)).ifPresent(order -> {
            boolean success = paymentGatewayService.charge(orderId);

            if (success) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                System.out.println("Order " + orderId + " -> PAID");
            } else {
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
                System.out.println("Order " + orderId + " -> FAILED. Payment gateway declined.");
                compensate(orderId);
            }
        });
    }

    private void compensate(String orderId) {
        System.out.println("Compensating transaction: releasing reserved inventory for order " + orderId);
    }
}