package org.mini_lab.file_upload_service.security.rate_limiter.service;


import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.enums.ErrorCode;
import org.mini_lab.file_upload_service.security.rate_limiter.component.CalculateWindowStarter;
import org.mini_lab.file_upload_service.security.rate_limiter.component.KeyGenerator;
import org.mini_lab.file_upload_service.security.rate_limiter.component.RateLimitValidator;
import org.mini_lab.file_upload_service.security.rate_limiter.exceptions.RateLimiterUnavailableException;
import org.mini_lab.file_upload_service.security.rate_limiter.properties.LoginRateLimitProperties;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.RedisLoginRateLimitRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service("redisLoginRateLimitService")
@RequiredArgsConstructor
public class RedisLoginRateLimitService implements LoginRateLimitService {

    private static final String KEY_PREFIX = "rate-limit:login:";

    private final RedisLoginRateLimitRepository repository;
    private final LoginRateLimitProperties properties;
    private final Clock clock;
    private final RateLimitValidator rateLimitValidator;
    private final KeyGenerator keyGenerator;
    private final CalculateWindowStarter calculateWindowStarter;

    @Override
    @WithSpan("redis-login-rate-limit-service-allow")
    public boolean allow(String identity) {
        Duration windowDuration = properties.getWindowDuration();
        int maxAttempts = properties.getUsernameIpMaxAttempts();

        rateLimitValidator.validateConfiguration(windowDuration, maxAttempts);

        Instant now = clock.instant();

        Instant windowStart = calculateWindowStarter.calculateWindowStart(now, windowDuration);

        Instant windowEnd = windowStart.plus(windowDuration);

        long remainingTtlMillis = Math.max(Duration.between(now, windowEnd).toMillis(), 1L);

        String key = keyGenerator.createKey(KEY_PREFIX, identity, windowStart.toEpochMilli());

        long currentAttempts;


        try {
            currentAttempts = repository.incrementAndSetExpiration(key, Duration.ofMillis(remainingTtlMillis));
        } catch (DataAccessException e) {
            throw new RateLimiterUnavailableException(ErrorCode.Rate_LIMITER_UNAVAILABLE.getDefaultMessage(), e);
        }

        return currentAttempts <= maxAttempts;
    }

}