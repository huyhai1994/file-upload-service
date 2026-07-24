package org.mini_lab.file_upload_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("file.file-delete-recovery")
@Getter
@Setter
public class FileDeleteRecoveryProperties {
    Duration deletingTimeout;
    Duration schedulerInterval;
    int batchSize;

}
