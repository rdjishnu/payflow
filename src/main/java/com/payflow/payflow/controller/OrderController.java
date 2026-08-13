package com.payflow.payflow.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
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
import com.payflow.payflow.service.IdempotencyService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final IdempotencyService idempotencyService;

    public OrderController(OrderRepository orderRepository, RabbitTemplate rabbitTemplate, IdempotencyService idempotencyService) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.idempotencyService = idempotencyService;
    }

    private static final String PENDING = "PENDING";

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest req) {
        String key = req.getIdempotencyKey();

        // Fast path: Redis reservation, atomic, no DB hit for duplicates
        Optional<String> reservation = idempotencyService.checkAndReserve(key, PENDING);

        if (reservation.isPresent()) {
            String value = reservation.get();
            if (PENDING.equals(value)) {
                // another request with this key is mid-flight right now
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            // value is an actual order ID from a completed request — return that order
            return orderRepository.findById(UUID.fromString(value))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // First time seeing this key — proceed with creation
        try {
            Order order = new Order();
            order.setCustomerId(req.getCustomerId());
            order.setAmount(req.getAmount());
            order.setStatus(OrderStatus.CREATED);
            order.setIdempotencyKey(key);
            order.setCreatedAt(Instant.now());
            Order saved = orderRepository.save(order);

            idempotencyService.confirm(key, saved.getId().toString());

            rabbitTemplate.convertAndSend(RabbitConfig.ORDER_QUEUE, saved.getId().toString());

            return ResponseEntity.ok(saved);
        } catch (RuntimeException | IOException e) {
            idempotencyService.release(key); // free the key so client can retry cleanly
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}