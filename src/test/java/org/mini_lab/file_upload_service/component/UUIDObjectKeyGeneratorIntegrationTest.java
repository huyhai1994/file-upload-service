package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class UUIDObjectKeyGeneratorIntegrationTest {
    @Test
    void shouldGenerateObjectKey() {

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-19T10:00:00Z"),
                ZoneOffset.UTC
        );

        UUIDObjectKeyGenerator generator =
                new UUIDObjectKeyGenerator(fixedClock);

        String key = generator.generate();

        assertTrue(key.startsWith("files/2026/06/"));
    }

}