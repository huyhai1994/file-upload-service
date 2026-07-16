package org.mini_lab.file_upload_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "minio")
public record MinioConfigProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName,
        Duration connectTimeout,
        Duration writeTimeout,
        Duration readTimeout) {
}