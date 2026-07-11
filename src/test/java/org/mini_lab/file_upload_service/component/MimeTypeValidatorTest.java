package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
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
class MimeTypeValidatorTest extends AbstractIntegrationTest {
    @Autowired
    MimeTypeValidator mimeTypeValidator;


    @Test
    void test() {
        assertDoesNotThrow(() -> mimeTypeValidator.validate(buildFileUploadCommand(createMultipartFile())));
    }

    private static MockMultipartFile createMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
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