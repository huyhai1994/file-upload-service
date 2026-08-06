package org.mini_lab.file_upload_service.security.rate_limiter.exceptions;


public class RateLimiterUnavailableException extends RuntimeException {

    public RateLimiterUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
