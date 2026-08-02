package com.payflow.payflow.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String customerId;
    private BigDecimal amount;
    private String idempotencyKey;
}