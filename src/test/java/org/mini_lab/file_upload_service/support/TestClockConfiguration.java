package org.mini_lab.file_upload_service.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
public class TestClockConfiguration {

    @Bean
    @Primary
    public Clock clock() {
        return Clock.fixed(
                Instant.parse("2026-07-08T10:00:00Z"),
                ZoneOffset.UTC
        );
    }
}