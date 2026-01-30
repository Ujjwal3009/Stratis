package com.upsc.ai.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Component
public class RateLimitingConfig {

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long userId) {
        return buckets.computeIfAbsent(userId, this::createNewBucket);
    }

    private Bucket createNewBucket(Long userId) {
        // Allow 5 AI requests per minute for a Series A/B startup standard
        // This prevents runaway costs while remaining usable
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }
}
