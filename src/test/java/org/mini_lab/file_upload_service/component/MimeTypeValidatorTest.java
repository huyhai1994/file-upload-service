package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getFileUploadCommand;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;

@SpringBootTest
@ActiveProfiles("test")
class MimeTypeValidatorTest extends AbstractIntegrationTest {
    @Autowired
    MimeTypeValidator mimeTypeValidator;


    @Test
    void test() {
        assertDoesNotThrow(() -> mimeTypeValidator.validate(getFileUploadCommand(getTextContentTypeMultipartFile())));
    }

}