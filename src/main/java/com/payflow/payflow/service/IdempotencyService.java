package com.payflow.payflow.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${payflow.idempotency.ttl-seconds:30}")
    private long ttlSeconds;

    private static final String PREFIX = "idempotency:order:";

    public Optional<String> checkAndReserve(String idempotencyKey, String placeholderValue) {
        String redisKey = PREFIX + idempotencyKey;
        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, placeholderValue, Duration.ofSeconds(ttlSeconds));

        if (Boolean.TRUE.equals(wasSet)) {
            return Optional.empty();
        }
        String existing = redisTemplate.opsForValue().get(redisKey);
        return Optional.ofNullable(existing);
    }

    public void confirm(String idempotencyKey, String orderId) {
        redisTemplate.opsForValue().set(PREFIX + idempotencyKey, orderId, Duration.ofSeconds(ttlSeconds));
    }

    public void release(String idempotencyKey) {
        redisTemplate.delete(PREFIX + idempotencyKey);
    }
}
