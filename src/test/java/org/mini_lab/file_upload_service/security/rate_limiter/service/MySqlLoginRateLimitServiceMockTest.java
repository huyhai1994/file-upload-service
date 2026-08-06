package org.mini_lab.file_upload_service.security.rate_limiter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.security.rate_limiter.component.CalculateWindowStarter;
import org.mini_lab.file_upload_service.security.rate_limiter.component.RateLimitValidator;
import org.mini_lab.file_upload_service.security.rate_limiter.properties.LoginRateLimitProperties;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.LoginRateLimitRepository;
import org.mini_lab.file_upload_service.support.MockTimeBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mini_lab.file_upload_service.support.MockLoginRequestBuilder.IDENTITY_HASH;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MySqlLoginRateLimitServiceMockTest {

    @InjectMocks
    private MySqlLoginRateLimitService mySqlLoginRateLimitService;

    @Mock
    private LoginRateLimitProperties loginRateLimitProperties;

    @Mock
    private LoginRateLimitRepository loginRateLimitRepository;

    @Mock
    private CalculateWindowStarter calculateWindowStarter;

    @Mock
    private RateLimitValidator rateLimitValidator;

    @Mock
    private Clock clock;

    @Test
    void allow_whenAttemptSurpassesLimit_thenReturnFalse() {
        // Given
        int maxAttempts = 5;
        int currentAttempts = 6;
        Instant now = MockTimeBuilder.NOW;
        Instant windowStart = now.truncatedTo(ChronoUnit.MINUTES);
        Duration windowDuration = Duration.ofMinutes(1);

        when(clock.instant()).thenReturn(now);

        when(loginRateLimitProperties.getUsernameIpMaxAttempts())
                .thenReturn(maxAttempts);

        when(loginRateLimitProperties.getWindowDuration())
                .thenReturn(windowDuration);

        when(calculateWindowStarter.calculateWindowStart(now, windowDuration)).thenReturn(windowStart);

        when(loginRateLimitRepository.incrementCounterAndReturnAffectedRows(
                IDENTITY_HASH,
                windowStart
        )).thenReturn(1);

        when(loginRateLimitRepository.findAttemptCount(
                IDENTITY_HASH,
                windowStart
        )).thenReturn(currentAttempts);

        boolean allowed = mySqlLoginRateLimitService.allow(IDENTITY_HASH);

        assertThat(allowed).isFalse();

        verify(rateLimitValidator)
                .validateConfiguration(windowDuration, maxAttempts);

        verify(loginRateLimitRepository)
                .incrementCounterAndReturnAffectedRows(
                        IDENTITY_HASH,
                        windowStart
                );

        verify(loginRateLimitRepository)
                .findAttemptCount(
                        IDENTITY_HASH,
                        windowStart
                );
    }

    @Test
    void allow_whenAttemptNotPassLimit_thenReturnTrue() {
        // Given
        int maxAttempts = 5;
        int currentAttempts = 3;
        Instant now = MockTimeBuilder.NOW;
        Instant windowStart = now.truncatedTo(ChronoUnit.MINUTES);
        Duration windowDuration = Duration.ofMinutes(1);

        when(clock.instant()).thenReturn(now);

        when(loginRateLimitProperties.getUsernameIpMaxAttempts())
                .thenReturn(maxAttempts);
        when(loginRateLimitProperties.getWindowDuration())
                .thenReturn(windowDuration);
        when(loginRateLimitRepository.incrementCounterAndReturnAffectedRows(
                IDENTITY_HASH,
                windowStart
        )).thenReturn(1);

        when(calculateWindowStarter.calculateWindowStart(now, windowDuration)).thenReturn(windowStart);

        when(loginRateLimitRepository.findAttemptCount(
                IDENTITY_HASH,
                windowStart
        )).thenReturn(currentAttempts);

        // When
        boolean allowed = mySqlLoginRateLimitService.allow(IDENTITY_HASH);

        // Then
        assertThat(allowed).isTrue();

        verify(loginRateLimitRepository)
                .incrementCounterAndReturnAffectedRows(
                        IDENTITY_HASH,
                        windowStart
                );

        verify(loginRateLimitRepository)
                .findAttemptCount(
                        IDENTITY_HASH,
                        windowStart
                );
    }
}