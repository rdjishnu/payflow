package com.payflow.payflow.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${payflow.rate-limit.capacity:10}")
    private long capacity;

    @Value("${payflow.rate-limit.refill-seconds:60}")
    private long windowSeconds;

    public boolean isAllowed(String clientKey) {
        String redisKey = "ratelimit:" + clientKey;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }
        return count != null && count <= capacity;
    }
}
