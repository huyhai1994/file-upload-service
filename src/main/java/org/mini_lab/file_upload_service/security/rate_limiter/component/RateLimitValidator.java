package org.mini_lab.file_upload_service.security.rate_limiter.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitValidator {

    public void validateConfiguration(
            Duration windowDuration,
            int maxAttempts
    ) {
        if (windowDuration == null
                || windowDuration.isZero()
                || windowDuration.isNegative()) {

            throw new IllegalStateException(
                    "Rate-limit window duration must be positive"
            );
        }

        if (maxAttempts <= 0) {
            throw new IllegalStateException(
                    "Rate-limit max attempts must be positive"
            );
        }
    }

}
