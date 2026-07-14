package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.exception.InvalidFileExtensionException;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;

@SpringBootTest
@ActiveProfiles("test")
class ExtensionValidatorTest extends AbstractIntegrationTest {

    @Autowired
    ExtensionValidator extensionValidator;

    @Test
    void whenExtensionInAllowedList_thenNotThrowException() {
        assertDoesNotThrow(() -> extensionValidator.validate(getFileUploadCommand(getTextContentTypeMultipartFile())));
    }

    @Test
    void whenExtensionIsNotInAllowedList_thenThrowsException() {
        assertThrows(InvalidFileExtensionException.class,
                () -> extensionValidator.validate(
                        getFileUploadCommand(getNonValidExtensionMultipartFile())));
    }

}