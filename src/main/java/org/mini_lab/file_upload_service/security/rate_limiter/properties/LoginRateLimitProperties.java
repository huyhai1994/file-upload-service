package org.mini_lab.file_upload_service.security.rate_limiter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "login-rate-limit")
@Getter
@Setter
public class LoginRateLimitProperties {
    Integer usernameIpMaxAttempts;
    Duration windowDuration;
}
