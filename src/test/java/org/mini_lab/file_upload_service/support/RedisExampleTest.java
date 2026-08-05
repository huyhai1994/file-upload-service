package org.mini_lab.file_upload_service.support;

import com.redis.testcontainers.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class RedisExampleTest {

    @Container
    private static final RedisContainer container = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

    @Test
    void testSomethingUsingLettuce() {
        String redisURI = container.getRedisURI();
        try (RedisClient client = RedisClient.create(redisURI)) {
            try (StatefulRedisConnection<String, String> connection = client.connect()) {
                RedisCommands<String, String> commands = connection.sync();
                Assertions.assertEquals("PONG", commands.ping());
            }
        }
    }
}
