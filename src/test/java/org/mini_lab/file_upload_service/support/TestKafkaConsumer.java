package org.mini_lab.file_upload_service.support;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class TestKafkaConsumer {

    public static ConsumerRecord<String, String> consumeOne(
            String bootstrapServers,
            String topic
    ) {
        Properties props = new Properties();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "integration-test-" + System.nanoTime()
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        try (KafkaConsumer<String, String> consumer =
                     new KafkaConsumer<>(props)) {

            consumer.subscribe(List.of(topic));

            long deadline = System.currentTimeMillis() + 5_000;

            while (System.currentTimeMillis() < deadline) {

                var records = consumer.poll(Duration.ofMillis(200));

                if (!records.isEmpty()) {
                    return records.iterator().next();
                }
            }

            throw new AssertionError(
                    "No Kafka message received from topic: " + topic
            );
        }
    }
}