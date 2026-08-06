package org.mini_lab.file_upload_service.security.rate_limiter.component;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class CalculateWindowStarter {
    public Instant calculateWindowStart(Instant now, Duration windowDuration) {
        long windowMillis = windowDuration.toMillis();

        long nowMillis = now.toEpochMilli();

        long windowStartMillis =
                Math.floorDiv(nowMillis, windowMillis) * windowMillis;

        return Instant.ofEpochMilli(windowStartMillis);

    }
}
