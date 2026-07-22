package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.DatabaseConnectionResetSimulator;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.CannotCreateTransactionException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class FileDeleteServiceIntergrationTest extends AbstractIntegrationTest {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileDeleteService fileDeleteService;

    @AfterEach
    void cleanUp() {
        fileMetadataRepository.deleteAllInBatch();
    }

    private long fileId = 0L;

    @BeforeEach
    void setUp() {
        FileMetadata fileMetadata = fileMetadataRepository.save(MockObjectBuilder.getValidCompletedFileMetadata());
        fileId = fileMetadata.getId();
    }


    @Test
    void processDeleteFile_whenDBShutdown_thenThrowsCannotCreateTransactionException() throws IOException {
        try (DatabaseConnectionResetSimulator ignored = new DatabaseConnectionResetSimulator(mysqlProxy)) {
            assertThrows(CannotCreateTransactionException.class, () -> fileDeleteService.processDeleteFile(fileId));
        }
    }


}