package org.mini_lab.file_upload_service.security.rate_limiter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisCounterRepository {

    private final StringRedisTemplate redisTemplate;

    public long increment(String key) {
        Long result = redisTemplate
                .opsForValue()
                .increment(key);

        if (result == null) {
            throw new IllegalStateException(
                    "Redis INCR returned null"
            );
        }

        return result;
    }

    public void expire(
            String key,
            Duration duration
    ) {
        Boolean result = redisTemplate.expire(
                key,
                duration
        );

        if (!Boolean.TRUE.equals(result)) {
            throw new IllegalStateException(
                    "Could not set expiration for key: " + key
            );
        }
    }

    public String get(String key) {
        return redisTemplate
                .opsForValue()
                .get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
}