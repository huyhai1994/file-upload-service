package org.mini_lab.file_upload_service.security.rate_limiter.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.rate_limiter.properties.LoginRateLimitProperties;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.LoginRateLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MySqlLoginRateLimitService implements LoginRateLimitService {

    private final LoginRateLimitRepository loginRateLimitRepository;

    private final LoginRateLimitProperties loginRateLimitProperties;

    private final Clock clock;

    @Override
    @Transactional
    public boolean allow(String identity) {
        Instant windowStart = calculateWindowStart();

        loginRateLimitRepository.incrementCounterAndReturnAffectedRows(
                identity,
                windowStart
        );

        int currentCount = loginRateLimitRepository.findAttemptCount(
                identity,
                windowStart
        );

        return currentCount <= loginRateLimitProperties.getUsernameIpMaxAttempts();
    }

    private Instant calculateWindowStart() {
        return Instant.now(clock)
                .truncatedTo(ChronoUnit.MINUTES);
    }
}
