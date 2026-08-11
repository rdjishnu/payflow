package com.payflow.payflow.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.payflow.config.RabbitConfig;
import com.payflow.payflow.dto.CreateOrderRequest;
import com.payflow.payflow.model.Order;
import com.payflow.payflow.model.OrderStatus;
import com.payflow.payflow.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest req) {
        Optional<Order> existing = orderRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        Order order = new Order();
        order.setCustomerId(req.getCustomerId());
        order.setAmount(req.getAmount());
        order.setStatus(OrderStatus.CREATED);
        order.setIdempotencyKey(req.getIdempotencyKey());
        order.setCreatedAt(Instant.now());
        Order saved = orderRepository.save(order);

        rabbitTemplate.convertAndSend(RabbitConfig.ORDER_QUEUE, saved.getId().toString());

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}