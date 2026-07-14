package org.mini_lab.file_upload_service.service;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getMismatchMimeFile;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getMockTextContentTypeMultipartFile;


class TikaMimeTypeExtractorTest {
    private final TikaMimeTypeExtractor tikaMimeTypeExtractor = new TikaMimeTypeExtractor(new Tika());

    @Test
    void whenExtractMimeType_returnTextPlain() {
        assertEquals("text/plain", tikaMimeTypeExtractor.extract(getMockTextContentTypeMultipartFile()));
    }

    @Test
    void whenExtensionIsTxtButContentIsPng_thenReturnImagePng() {
        assertEquals("image/png", tikaMimeTypeExtractor.extract(getMismatchMimeFile()));
    }


}