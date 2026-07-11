package org.mini_lab.file_upload_service.service;

import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


class TikaMimeTypeExtractorTest {
    private final TikaMimeTypeExtractor tikaMimeTypeExtractor = new TikaMimeTypeExtractor(new Tika());

    @Test
    void whenExtractMimeType_returnTextPlain() {
        assertEquals("text/plain", tikaMimeTypeExtractor.extract(createValidMimeTypeFile()));
    }

    @Test
    void whenExtensionIsTxtButContentIsPng_thenReturnImagePng() {
        MockMultipartFile file = getMismatchMimeFile();

        assertEquals("image/png", tikaMimeTypeExtractor.extract(file));
    }

    private static @NotNull MockMultipartFile getMismatchMimeFile() {
        byte[] pngHeader = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };

        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                pngHeader
        );
    }

    private static MockMultipartFile createValidMimeTypeFile() {
        return new MockMultipartFile(
                "file", "test.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

}