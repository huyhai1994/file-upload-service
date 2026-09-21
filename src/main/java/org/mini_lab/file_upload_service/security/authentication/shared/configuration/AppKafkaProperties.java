package org.mini_lab.file_upload_service.security.authentication.shared.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class AppKafkaProperties {

    private List<String> bootstrapServers;
    private String clientId;
    private Producer producer = new Producer();

    @Getter
    @Setter
    public static class Producer {

        private String keySerializer;
        private String valueSerializer;
        private String acks;

        private Map<String, Object> properties = new HashMap<>();
    }
}
