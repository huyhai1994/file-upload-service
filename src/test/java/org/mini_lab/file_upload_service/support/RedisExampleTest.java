package org.mini_lab.file_upload_service.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisExampleTest extends AbstractIntegrationTest {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Test
    void shouldConnectToRedisThroughToxiproxy() {
        try (RedisConnection connection =
                     requireNonNull(connectionFactory).getConnection()) {

            String result = connection.ping();

            assertThat(result).isEqualTo("PONG");
        }
    }
}
