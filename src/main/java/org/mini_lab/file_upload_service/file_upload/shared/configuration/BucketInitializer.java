package org.mini_lab.file_upload_service.file_upload.shared.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BucketInitializer {

    private final MinioClient minioClient;
    private final MinioConfigProperties properties;

    @PostConstruct
    void init() throws Exception {
        log.info("INIT endpoint: {}", properties.endpoint());
        if (!minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(properties.bucketName())
                        .build())) {

            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(properties.bucketName())
                            .build());
        }
    }
}