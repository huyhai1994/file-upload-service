package org.mini_lab.file_upload_service.service;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.service.extractor.TikaMimeTypeExtractor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getMismatchMimeMultipartFile;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;


class TikaMimeTypeExtractorTest {
    private final TikaMimeTypeExtractor tikaMimeTypeExtractor = new TikaMimeTypeExtractor(new Tika());

    @Test
    void whenExtractMimeType_returnTextPlain() {
        assertEquals("text/plain", tikaMimeTypeExtractor.extract(getTextContentTypeMultipartFile()));
    }

    @Test
    void whenExtensionIsTxtButContentIsPng_thenReturnImagePng() {
        assertEquals("image/png", tikaMimeTypeExtractor.extract(getMismatchMimeMultipartFile()));
    }


}