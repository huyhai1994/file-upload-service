package org.mini_lab.file_upload_service.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BucketInitializer {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    void init() throws Exception {

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