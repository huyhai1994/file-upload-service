package org.mini_lab.file_upload_service.security.rate_limiter.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.rate_limiter.component.CalculateWindowStarter;
import org.mini_lab.file_upload_service.security.rate_limiter.component.RateLimitValidator;
import org.mini_lab.file_upload_service.security.rate_limiter.properties.LoginRateLimitProperties;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.LoginRateLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service("mySqlLoginRateLimitService")
@RequiredArgsConstructor
public class MySqlLoginRateLimitService implements LoginRateLimitService {

    private final LoginRateLimitRepository loginRateLimitRepository;

    private final LoginRateLimitProperties loginRateLimitProperties;

    private final Clock clock;

    private final CalculateWindowStarter calculateWindowStarter;

    private final RateLimitValidator rateLimitValidator;

    @Override
    @Transactional
    public boolean allow(String identity) {

        Duration windowDuration = loginRateLimitProperties.getWindowDuration();

        int maxAttempts = loginRateLimitProperties.getUsernameIpMaxAttempts();

        rateLimitValidator.validateConfiguration(windowDuration, maxAttempts);

        Instant now = clock.instant();

        Instant windowStart = calculateWindowStarter.calculateWindowStart(now, loginRateLimitProperties.getWindowDuration());

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

}
