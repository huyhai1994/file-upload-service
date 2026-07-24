package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionExtractorTest {

    private final ExtensionExtractor extensionExtractor
            = new ExtensionExtractor();

    @Test
    void extract_whenFileNameValid_thenReturnExtension() {
        assertEquals(Optional.of("doc"), extensionExtractor.extract("test.doc"));
    }

    @Test
    void extract_whenFileNameNull_thenReturnEmpty() {
        assertEquals(Optional.empty(), extensionExtractor.extract(""));
    }


}