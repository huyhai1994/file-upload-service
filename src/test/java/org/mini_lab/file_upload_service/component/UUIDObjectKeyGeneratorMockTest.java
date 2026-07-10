package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UUIDObjectKeyGeneratorMockTest {

    @Mock
    Clock clock;

    @InjectMocks
    UUIDObjectKeyGenerator generator;

    @Test
    void shouldGenerateObjectKey() {

        when(clock.instant())
                .thenReturn(Instant.parse("2026-06-19T10:00:00Z"));

        when(clock.getZone())
                .thenReturn(ZoneOffset.UTC);

        String key = generator.generate();

        assertTrue(key.startsWith("files/2026/06/"));

        verify(clock).instant();
        verify(clock).getZone();
    }

}