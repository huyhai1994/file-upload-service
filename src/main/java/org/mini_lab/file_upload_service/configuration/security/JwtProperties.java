package org.mini_lab.file_upload_service.configuration.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secretKey,
        Duration accessTokenExpiration,
        String issuer
) {
}