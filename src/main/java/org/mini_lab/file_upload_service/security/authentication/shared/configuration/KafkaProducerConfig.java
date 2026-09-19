package org.mini_lab.file_upload_service.security.authentication.shared.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AppKafkaProperties.class)
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, String> producerFactory(
            AppKafkaProperties kafkaProperties
    ) {
        Map<String, Object> props = new HashMap<>();

        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()
        );

        props.put(
                ProducerConfig.CLIENT_ID_CONFIG,
                kafkaProperties.getClientId()
        );

        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                kafkaProperties.getProducer().getKeySerializer()
        );

        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                kafkaProperties.getProducer().getValueSerializer()
        );

        props.put(
                ProducerConfig.ACKS_CONFIG,
                kafkaProperties.getProducer().getAcks()
        );

        props.putAll(
                kafkaProperties.getProducer().getProperties()
        );

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

}
