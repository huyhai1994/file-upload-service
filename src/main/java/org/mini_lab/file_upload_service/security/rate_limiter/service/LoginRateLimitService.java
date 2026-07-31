package org.mini_lab.file_upload_service.security.rate_limiter.service;

public interface LoginRateLimitService {
    boolean allow(String identity);
}
