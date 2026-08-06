package org.mini_lab.file_upload_service.security.rate_limiter.repository;

import java.time.Duration;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisLoginRateLimitRepository {

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long>
            incrementWithExpirationScript;

    @WithSpan("redis-login-rate-limit-repository-increment-and-set-expiration")
    public long incrementAndSetExpiration(
            String key,
            Duration ttl
    ) {
        return redisTemplate.execute(
                incrementWithExpirationScript,
                List.of(key),
                Long.toString(ttl.toMillis())
        );
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
}