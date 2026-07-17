package org.mini_lab.file_upload_service.support;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.toxiproxy.ToxiproxyContainer;

import java.io.IOException;

@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final String MYSQL_DATABASE_NAME =
            "file_upload_service_test";

    private static final String MINIO_BUCKET_NAME =
            "file-upload-test";

    private static final int MYSQL_PROXY_PORT = 8666;
    private static final int MINIO_PROXY_PORT = 8667;

    private static final Network NETWORK =
            Network.newNetwork();

    private static final MySQLContainer mysqlDb;

    private static final MinIOContainer minioStorage;

    private static final ToxiproxyContainer toxiproxyContainer;

    protected static Proxy mysqlProxy;
    protected static Proxy minioProxy;

    static {
        mysqlDb = new MySQLContainer("mysql:8.0")
                .withDatabaseName(MYSQL_DATABASE_NAME)
                .withUsername("test")
                .withPassword("test")
                .withNetwork(NETWORK)
                .withNetworkAliases("mysql");

        minioStorage = new MinIOContainer(
                "minio/minio:RELEASE.2023-09-04T19-57-37Z"
        )
                .withUserName("testuser")
                .withPassword("testpassword")
                .withNetwork(NETWORK)
                .withNetworkAliases("minio");

        toxiproxyContainer = new ToxiproxyContainer(
                "ghcr.io/shopify/toxiproxy:2.5.0"
        ).withNetwork(NETWORK);

        mysqlDb.start();
        minioStorage.start();
        toxiproxyContainer.start();

        createProxies();
    }

    private static void createProxies() {
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient(
                toxiproxyContainer.getHost(),
                toxiproxyContainer.getControlPort()
        );

        try {
            mysqlProxy = toxiproxyClient.createProxy(
                    "mysql",
                    "0.0.0.0:" + MYSQL_PROXY_PORT,
                    "mysql:3306"
            );

            minioProxy = toxiproxyClient.createProxy(
                    "minio",
                    "0.0.0.0:" + MINIO_PROXY_PORT,
                    "minio:9000"
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not create Toxiproxy proxies",
                    exception
            );
        }
    }

    @DynamicPropertySource
    static void registerProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.username",
                mysqlDb::getUsername
        );

        registry.add(
                "spring.datasource.password",
                mysqlDb::getPassword
        );

        registry.add(
                "spring.datasource.url",
                () -> String.format(
                        "jdbc:mysql://%s:%d/%s"
                                + "?connectTimeout=%d"
                                + "&socketTimeout=%d"
                                + "&tcpKeepAlive=%b",
                        toxiproxyContainer.getHost(),
                        toxiproxyContainer.getMappedPort(
                                MYSQL_PROXY_PORT
                        ),
                        MYSQL_DATABASE_NAME,
                        5_000,
                        5_000,
                        false
                )
        );

        registry.add(
                "minio.access-key",
                minioStorage::getUserName
        );

        registry.add(
                "minio.secret-key",
                minioStorage::getPassword
        );

        registry.add(
                "minio.bucket-name",
                () -> MINIO_BUCKET_NAME
        );

        registry.add(
                "minio.endpoint",
                () -> String.format(
                        "http://%s:%d",
                        toxiproxyContainer.getHost(),
                        toxiproxyContainer.getMappedPort(
                                MINIO_PROXY_PORT
                        )
                )
        );
    }
}