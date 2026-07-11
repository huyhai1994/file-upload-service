package org.mini_lab.file_upload_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("file.extension")
@Getter
@Setter
public class ExtensionPropertiesConfigurations {
    private List<String> allowedList;
}
