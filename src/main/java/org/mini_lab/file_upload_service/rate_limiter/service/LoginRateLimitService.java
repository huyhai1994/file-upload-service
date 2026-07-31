package org.mini_lab.file_upload_service.rate_limiter.service;

public interface LoginRateLimitService {
    boolean allow(String identity);
}
