package org.mini_lab.file_upload_service.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class AbstractIntegrationTest {
    private static final MySQLContainer mysqldb;
    private static final MinIOContainer miniostorage;

    static {
        mysqldb = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("file_upload_service_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

        mysqldb.start();

        miniostorage = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
                .withUserName("testuser")
                .withPassword("testpassword")
                .withReuse(true);
        miniostorage.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqldb::getJdbcUrl);
        registry.add("spring.datasource.username", mysqldb::getUsername);
        registry.add("spring.datasource.password", mysqldb::getPassword);

        registry.add("minio.endpoint", miniostorage::getS3URL);
        registry.add("minio.access-key", miniostorage::getUserName);
        registry.add("minio.secret-key", miniostorage::getPassword);
        registry.add("minio.bucket-name", () -> "file-upload-test");
    }
}

