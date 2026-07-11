package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.InvalidFileExtensionException;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ExtensionValidatorTest extends AbstractIntegrationTest {

    @Autowired
    ExtensionValidator extensionValidator;

    @Test
    void whenExtensionInAllowedList_thenNotThrowException() {
        assertDoesNotThrow(() -> extensionValidator.validate(buildFileUploadCommand(createValidExtensionMultipartFile())));
    }

    @Test
    void whenExtensionIsNotInAllowedList_thenThrowsException() {
        assertThrows(InvalidFileExtensionException.class,
                () -> extensionValidator.validate(
                        buildFileUploadCommand(createNonValidExtensionMultipartFile())));
    }

    private static MockMultipartFile createValidExtensionMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MockMultipartFile createNonValidExtensionMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.sh",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static FileUploadCommand buildFileUploadCommand(
            MultipartFile multipartFile
    ) {
        return FileUploadCommand.builder()
                .file(multipartFile)
                .contentType(multipartFile.getContentType())
                .originalFileName(multipartFile.getOriginalFilename())
                .build();
    }

}