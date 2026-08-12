package com.payflow.payflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

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
