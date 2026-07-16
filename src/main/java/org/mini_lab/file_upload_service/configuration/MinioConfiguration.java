package org.mini_lab.file_upload_service.configuration;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioConfigProperties.class)
@RequiredArgsConstructor
public class MinioConfiguration {

    private final MinioConfigProperties properties;

    @Bean
    OkHttpClient minioHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.connectTimeout())
                .writeTimeout(properties.writeTimeout())
                .readTimeout(properties.readTimeout())
                .build();
    }

    @Bean
    MinioClient minioClient(OkHttpClient minioHttpClient) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(
                        properties.accessKey(),
                        properties.secretKey()
                )
                .httpClient(minioHttpClient)
                .build();
    }
}