package com.casino.gateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // In-memory cache for buckets
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    // Rate limits
    private static final int PER_USER_REQUESTS = 100; // per minute
    private static final int PER_IP_REQUESTS = 1000; // per minute
    private static final int AUTH_REQUESTS = 10; // per minute

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            // Check IP-based rate limit
            if (!checkIpRateLimit(ip)) {
                log.warn("IP rate limit exceeded for: {}", ip);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            // Check user-based rate limit (if authenticated)
            if (userId != null && !checkUserRateLimit(userId)) {
                log.warn("User rate limit exceeded for: {}", userId);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            // Special rate limit for auth endpoints
            if (path.startsWith("/auth/") && !checkAuthRateLimit(ip)) {
                log.warn("Auth rate limit exceeded for: {}", ip);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    private boolean checkIpRateLimit(String ip) {
        String key = "rate:ip:" + ip;
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(PER_IP_REQUESTS));
        return bucket.tryConsume(1);
    }

    private boolean checkUserRateLimit(String userId) {
        String key = "rate:user:" + userId;
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(PER_USER_REQUESTS));
        return bucket.tryConsume(1);
    }

    private boolean checkAuthRateLimit(String ip) {
        String key = "rate:auth:" + ip;
        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(AUTH_REQUESTS));
        return bucket.tryConsume(1);
    }

    private Bucket createBucket(int capacity) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
