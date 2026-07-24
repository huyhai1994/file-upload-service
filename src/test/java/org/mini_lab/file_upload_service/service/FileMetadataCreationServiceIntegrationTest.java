package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.upload.FileMetadataCreationService;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getFileUploadCommand;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;

@SpringBootTest
@ActiveProfiles("test")
class FileMetadataCreationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    FileMetadataCreationService fileMetadataCreationService;

    @AfterEach
    void tearDown() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @Test
    void saveMetadataSuccess() {
        FileUploadCommand command = getFileUploadCommand(getTextContentTypeMultipartFile());
        FileMetadata fileMetadata = fileMetadataCreationService.createUploadingMetadata(command);
        assertNotNull(fileMetadata.getObjectKey());
    }

}