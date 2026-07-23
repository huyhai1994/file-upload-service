package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.delete.FileDeleteService;
import org.mini_lab.file_upload_service.service.s3.ObjectStorageClient;
import org.mini_lab.file_upload_service.service.state_manager.FileMetadataStateManager;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.ExternalServiceConnectionResetSimulator;
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

    @MockitoSpyBean
    private ObjectStorageClient objectStorageClient;


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
        try (ExternalServiceConnectionResetSimulator ignored = new ExternalServiceConnectionResetSimulator(mysqlProxy)) {
            assertThrows(CannotCreateTransactionException.class, () -> fileDeleteService.processDeleteFile(fileId));
        }
    }

    @Test
    void processDeleteFile_whenMarkDeletingThenDbConnectionReset_thenThrowsCannotCreateTransactionException() {
        doAnswer(invocation -> {
            try (ExternalServiceConnectionResetSimulator ignored = new ExternalServiceConnectionResetSimulator(mysqlProxy)) {
                return invocation.callRealMethod();
            }
        }).when(fileMetadataStateManager).markDeleting(fileId);

        assertThrows(
                JpaSystemException.class,
                () -> fileDeleteService.processDeleteFile(fileId)
        );

    }


    @Test
    void processDeleteFile_whenMinioConnectionReset_thenKeepsDeletingState() {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow();

        doAnswer(invocation -> {
            try (ExternalServiceConnectionResetSimulator ignored =
                         new ExternalServiceConnectionResetSimulator(minioProxy)) {

                return invocation.callRealMethod();
            }
        }).when(objectStorageClient).delete(metadata.getObjectKey());

        assertThrows(
                ObjectStorageException.class,
                () -> fileDeleteService.processDeleteFile(fileId)
        );

        FileMetadata actual = fileMetadataRepository.findById(fileId)
                .orElseThrow();

        assertEquals(FileState.DELETING, actual.getStatus());
    }
}