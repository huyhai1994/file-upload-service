package org.mini_lab.file_upload_service.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadRequestExtractorTest {
    private final FileUploadRequestExtractor fileUploadRequestExtractor = new FileUploadRequestExtractor();

    @Test
    void shouldBeExtractToFileUploadCommand() {
        UploadRequestObjectDTO uploadRequestObjectDTO = new UploadRequestObjectDTO(getMockMultipartFile());
        FileUploadCommand uploadCommand = fileUploadRequestExtractor.extract(uploadRequestObjectDTO);
        assertEquals("test.txt", uploadCommand.originalFileName());
        assertEquals("text/plain", uploadCommand.contentType());
    }

    private static @NotNull MockMultipartFile getMockMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello MinIO".getBytes(StandardCharsets.UTF_8)
        );
    }

}