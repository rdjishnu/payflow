package com.payflow.payflow.service;

import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class PaymentGatewayService {

    @Retry(name = "paymentGateway")
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "fallback")
    public boolean charge(String orderId) {
        System.out.println("Calling payment gateway for order " + orderId + "...");

        boolean success = Math.random() > 0.5; // ~50% fail rate — makes retry/breaker visible while testing
        if (!success) {
            throw new PaymentGatewayException("Gateway declined/timed out for order " + orderId);
        }
        return true;
    }

    // Signature must match: same args as the original method + Throwable at the end
    private boolean fallback(String orderId, Throwable t) {
        System.out.println("Fallback for order " + orderId + " -> "
                + t.getClass().getSimpleName() + ": " + t.getMessage());
        return false;
    }
}