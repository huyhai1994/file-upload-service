package org.mini_lab.file_upload_service.file_upload.shared.configuration;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.hibernate.query.results.internal.TableGroupImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(MinioConfigProperties.class)
@RequiredArgsConstructor
public class MinioConfiguration {

    private final MinioConfigProperties properties;

    @Bean
    OkHttpClient minioHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(properties.connectTimeout().getSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.writeTimeout().getSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.readTimeout().getSeconds(), TimeUnit.SECONDS)
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