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
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.CannotCreateTransactionException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;


@SpringBootTest
@ActiveProfiles("test")
class FileDeleteServiceIntergrationTest extends AbstractIntegrationTest {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileDeleteService fileDeleteService;

    @MockitoSpyBean
    private FileMetadataStateManager fileMetadataStateManager;

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
    void processDeleteFile_whenDBConnectionReset_thenThrowsCannotCreateTransactionException() throws IOException {
        try (DatabaseConnectionResetSimulator ignored = new DatabaseConnectionResetSimulator(mysqlProxy)) {
            assertThrows(CannotCreateTransactionException.class, () -> fileDeleteService.processDeleteFile(fileId));
        }
    }

    @Test
    void processDeleteFile_whenMarkDeletingThenDbConnectionReset_thenThrowsCannotCreateTransactionException() {
        doAnswer(invocation -> {
            try (DatabaseConnectionResetSimulator ignored = new DatabaseConnectionResetSimulator(mysqlProxy)) {
                return invocation.callRealMethod();
            }
        }).when(fileMetadataStateManager).markDeleting(fileId);

        assertThrows(
                JpaSystemException.class,
                () -> fileDeleteService.processDeleteFile(fileId)
        );

    }


}