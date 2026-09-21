package org.mini_lab.file_upload_service.security.notification.configuration.rest_client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("clients.notification-service")
public record NotificationClientProperties(
        String baseUrl,
        String baseUri,
        Duration connectTimeout,
        Duration readTimeout
) {
}
