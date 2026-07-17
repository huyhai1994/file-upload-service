package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.exception.InvalidMimeTypeException;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;

@SpringBootTest
@ActiveProfiles("test")
class MimeTypeValidatorTest extends AbstractIntegrationTest {
    @Autowired
    MimeTypeValidator mimeTypeValidator;

    @Test
    void whenExtensionAndContentNotMatch_shouldThrowException() {
        assertThrows(InvalidMimeTypeException.class, () -> mimeTypeValidator.validate(getFileUploadCommand(getMismatchMimeMultipartFile())));
    }

    @Test
    void readMimeTypeOfFile_shouldNotThrowException() {
        assertDoesNotThrow(() -> mimeTypeValidator.validate(getFileUploadCommand(getTextContentTypeMultipartFile())));
    }


}