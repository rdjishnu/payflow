package com.payflow.payflow.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue
    private UUID id;

    private String customerId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

   @Column(nullable = false, unique = true)
private String idempotencyKey;
    private Instant createdAt;
}