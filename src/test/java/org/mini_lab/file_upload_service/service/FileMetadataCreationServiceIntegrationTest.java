package org.mini_lab.file_upload_service.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FileMetadataCreationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    FileMetadataCreationService fileMetadataCreationService;

    @Autowired
    EntityManager entityManager;

    @AfterEach
    void tearDown() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @Test
    void saveMetadataSuccess() {
        FileUploadCommand command = buildFileUploadCommand();
        FileMetadata fileMetadata = fileMetadataCreationService.createUploadingMetadata(command);
        assertNotNull(fileMetadata.getObjectKey());
    }

    private FileUploadCommand buildFileUploadCommand() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello minio".getBytes(StandardCharsets.UTF_8)
        );

        return FileUploadCommand.builder()
                .file(file)
                .contentType(file.getContentType())
                .originalFileName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }
}