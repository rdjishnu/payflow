package com.payflow.payflow.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate; // <-- Added this import
import org.springframework.cache.annotation.Cacheable;
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
import com.payflow.payflow.service.NotificationPublisher;
import com.payflow.payflow.service.RateLimiterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final IdempotencyService idempotencyService;
    private final RateLimiterService rateLimiterService;
    private final NotificationPublisher notificationPublisher;

    public OrderController(OrderRepository orderRepository, RabbitTemplate rabbitTemplate, IdempotencyService idempotencyService, RateLimiterService rateLimiterService, NotificationPublisher notificationPublisher) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.idempotencyService = idempotencyService;
        this.rateLimiterService = rateLimiterService;
        this.notificationPublisher = notificationPublisher;
    }

    private static final String PENDING = "PENDING";

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest req) { // <-- Added @Valid here
        String clientKey = req.getCustomerId() != null ? req.getCustomerId().toString() : "default";

        if (!rateLimiterService.isAllowed(clientKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        String key = req.getIdempotencyKey();

        Optional<String> reservation = idempotencyService.checkAndReserve(key, PENDING);

        if (reservation.isPresent()) {
            String value = reservation.get();
            if (PENDING.equals(value)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return orderRepository.findById(UUID.fromString(value))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

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
            notificationPublisher.sendNotification(saved.getId().toString(), "CREATED");

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            idempotencyService.release(key);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Cacheable(value = "orders", key = "#id")
    public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}