package org.mini_lab.file_upload_service.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class AbstractIntegrationTest {

    private static final MySQLContainer<?> mysqldb;
    private static final MinIOContainer miniostorage;
    private static final Network NETWORK = Network.newNetwork();
    private static final ToxiproxyContainer toxiProxyContainer;

    protected static ToxiproxyContainer.ContainerProxy minioProxy;
    protected static ToxiproxyContainer.ContainerProxy mysqlProxy;

    private static final String MYSQL_DATABASE_NAME = "file_upload_service_test";
    private static final String MINIO_BUCKET_NAME = "file-upload-test";


    static {
        mysqldb = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName(MYSQL_DATABASE_NAME)
                .withNetworkAliases("mysql")
                .withNetwork(NETWORK)
                .withExposedPorts(3306)
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

        mysqldb.start();

        miniostorage = new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2023-09-04T19-57-37Z"))
                .withNetwork(NETWORK)
                .withNetworkAliases("minio")
                .withExposedPorts(9000)
                .withUserName("testuser")
                .withPassword("testpassword")
                .withReuse(true);

        miniostorage.start();

        toxiProxyContainer = new ToxiproxyContainer(
                DockerImageName.parse(
                        "ghcr.io/shopify/toxiproxy:2.5.0"
                )
        ).withNetwork(NETWORK);

        toxiProxyContainer.start();

        minioProxy = toxiProxyContainer.getProxy(
                miniostorage,
                9000
        );
        mysqlProxy = toxiProxyContainer.getProxy(
                mysqldb,
                3306
        );
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", mysqldb::getUsername);
        registry.add("spring.datasource.password", mysqldb::getPassword);
        registry.add(
                "spring.datasource.url",
                () -> String.format("jdbc:mysql://%s:%d/%s?connectTimeout=%d&socketTimeout=%d&tcpKeepAlive=%b",
                        toxiProxyContainer.getHost(),
                        mysqlProxy.getProxyPort(),
                        MYSQL_DATABASE_NAME,
                        5000,
                        5000,
                        false)
        );
        registry.add("minio.access-key", miniostorage::getUserName);
        registry.add("minio.secret-key", miniostorage::getPassword);
        registry.add("minio.bucket-name", () -> MINIO_BUCKET_NAME);
        registry.add(
                "minio.endpoint",
                () -> String.format("http://%s:%d", toxiProxyContainer.getHost(), minioProxy.getProxyPort())
        );
    }

}