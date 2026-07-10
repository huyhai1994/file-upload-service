package org.mini_lab.file_upload_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioConfigProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName
) {
}