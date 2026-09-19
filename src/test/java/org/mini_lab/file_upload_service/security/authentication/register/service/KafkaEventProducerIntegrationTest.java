package org.mini_lab.file_upload_service.security.authentication.register.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.TestKafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class KafkaEventProducerIntegrationTest extends AbstractIntegrationTest {

    public static final String TOPIC = "test.topic";
    public static final String KEY = "user-1";
    public static final String PAYLOAD = """
            {
              "event": "USER_REGISTERED"
            }
            """;
    @Autowired
    KafkaEventProducer kafkaEventProducer;

    @Test
    void send_whenEventSentSuccess_thenVerifyTopicMetadata() throws ExecutionException, InterruptedException, TimeoutException {

        CompletableFuture<SendResult<String, String>> future =
                kafkaEventProducer.send(
                        TOPIC,
                        KEY,
                        PAYLOAD
                );

        SendResult<String, String> result =
                future.get(5, TimeUnit.SECONDS);

        assertThat(result).isNotNull();

        assertThat(
                result.getRecordMetadata().topic()
        ).isEqualTo(TOPIC);

        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS);

        ConsumerRecord<String, String> record = TestKafkaConsumer.consumeOne(kafka.getBootstrapServers()
                , TOPIC);

        assertThat(record.topic()).isEqualTo(TOPIC);

        assertThat(record.key())
                .isEqualTo(KEY);

        assertThat(record.value())
                .isEqualTo(PAYLOAD);

    }

}