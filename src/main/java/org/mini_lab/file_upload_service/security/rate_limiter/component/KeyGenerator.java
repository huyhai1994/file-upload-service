package org.mini_lab.file_upload_service.security.rate_limiter.component;

import org.springframework.stereotype.Component;

@Component
public class KeyGenerator {
    public String createKey(
            String keyPrefix,
            String identity,
            long windowStartMillis
    ) {
        return keyPrefix
                + identity
                + ":"
                + windowStartMillis;
    }
}
