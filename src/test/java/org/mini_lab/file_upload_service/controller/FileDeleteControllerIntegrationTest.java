package org.mini_lab.file_upload_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.s3.ObjectStorageClient;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileDeleteControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String DELETE_FILE_URL = "/api/v1/files/{fileId}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private ObjectStorageClient objectStorageClient;

    @BeforeEach
    void setUp() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @Test
    void deleteFile_whenFileIsCompleted_shouldDeleteFileAndReturnSuccess()
            throws Exception {

        // Arrange
        FileMetadata persistedFile = createCompletedFileWithStoredObject();

        Long fileId = persistedFile.getId();
        String objectKey = persistedFile.getObjectKey();

        // Act & Assert: HTTP response
        mockMvc.perform(delete(DELETE_FILE_URL, fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.success").value(true));

        // Assert: database state
        FileMetadata deletedFile = fileMetadataRepository.findById(fileId)
                .orElseThrow();

        assertThat(deletedFile.getStatus())
                .isEqualTo(FileState.DELETED);

        // Assert: object storage state
        assertThat(objectStorageClient.exists(objectKey))
                .isFalse();
    }

    private FileMetadata createCompletedFileWithStoredObject() {
        var multipartFile = MockObjectBuilder.getTextContentTypeMultipartFile();

        FileMetadata fileMetadata = MockObjectBuilder.getValidCompletedFileMetadata();

        fileMetadata.setFileName(multipartFile.getOriginalFilename());
        fileMetadata.setContentType(multipartFile.getContentType());
        fileMetadata.setSize(multipartFile.getSize());
        fileMetadata.setExtension("txt");

        FileMetadata persistedFile =
                fileMetadataRepository.saveAndFlush(fileMetadata);

        FileUploadCommand uploadCommand = FileUploadCommand.builder()
                .title(persistedFile.getTitle())
                .originalFileName(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .size(multipartFile.getSize())
                .file(multipartFile)
                .build();

        objectStorageClient.upload(
                persistedFile.getObjectKey(),
                uploadCommand
        );

        return persistedFile;
    }
}