package org.mini_lab.file_upload_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SystemTimeConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
