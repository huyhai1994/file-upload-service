package org.mini_lab.file_upload_service.security.authentication.register.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user-register")
@Getter
@Setter
public class NotificationRegisterEventProperties {
    private String topic;
}
