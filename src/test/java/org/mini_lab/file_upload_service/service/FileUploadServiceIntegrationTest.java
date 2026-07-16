package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.InternalServerException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.mini_lab.file_upload_service.support.TrafficBlockedSimulationTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class FileUploadServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @MockitoSpyBean
    FileMetadataCreationService fileMetadataCreationService;

    @MockitoSpyBean
    FileUploadStateManager fileUploadStateManager;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioConfigProperties minioConfigProperties;

    @AfterEach()
    void tearDown() {
        fileMetadataRepository.deleteAllInBatch();
    }

    public StatObjectResponse getUploadedFile(String objectKey) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfigProperties.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

//    @Test
//    void whenMySqlTrafficIsBlocked_thenRequestFailsBySocketTimeout() {
//        mysqlProxy.setConnectionCut(true);
//        try {
//            assertThrows(
//                    InternalServerException.class,
//                    () -> fileUploadService.processUploadFile(
//                            new UploadRequestObjectDTO(MockObjectBuilder.getTextContentTypeMultipartFile()))
//            );
//        } finally {
//            mysqlProxy.setConnectionCut(false);
//        }
//    }

    @Test
    void whenUploadSuccess_thenChangeStateToCompleted() {
        FileMetadataResponseDTO fileMetadataResponseDTO = fileUploadService.processUploadFile(new UploadRequestObjectDTO(MockObjectBuilder.getTextContentTypeMultipartFile()));
        FileMetadata persistedFileMetadata = fileMetadataRepository.findById(fileMetadataResponseDTO.fileId()).orElseThrow();
        assertEquals(FileState.COMPLETED, persistedFileMetadata.getStatus());
        assertNotNull(persistedFileMetadata.getChecksum());
        StatObjectResponse stat = getUploadedFile(persistedFileMetadata.getObjectKey());
        assertEquals(stat.size(), persistedFileMetadata.getSize());
    }

    @Test
    void whenMinIoTrafficIsBlocked_thenObjectNotInMinIOAndStateUpdatedToFailed()
            throws IOException {

        AtomicReference<Long> metadataId = new AtomicReference<>();

        doAnswer(invocation -> {
            FileMetadata metadata = (FileMetadata) invocation.callRealMethod();

            metadataId.set(metadata.getId());

            return metadata;
        }).when(fileMetadataCreationService)
                .createUploadingMetadata(any(FileUploadCommand.class));

        try (TrafficBlockedSimulationTools ignored =
                     TrafficBlockedSimulationTools.applyTo(minioProxy)) {

            assertThrows(
                    InternalServerException.class,
                    () -> fileUploadService.processUploadFile(
                            new UploadRequestObjectDTO(
                                    MockObjectBuilder.getTextContentTypeMultipartFile()
                            )
                    )
            );
        }

        FileMetadata metadata = fileMetadataRepository
                .findById(metadataId.get())
                .orElseThrow();

        assertEquals(FileState.FAILED, metadata.getStatus());
        assertThrows(RuntimeException.class, () -> getUploadedFile(metadata.getObjectKey()));
    }

    @Test
    void whenMarkCompletedFails_thenDeleteObjectAndMarkMetadataFailed() {

        AtomicReference<Long> metadataId = new AtomicReference<>();

        doAnswer(invocation -> {
            FileMetadata metadata = (FileMetadata) invocation.callRealMethod();
            metadataId.set(metadata.getId());
            return metadata;
        })
                .when(fileMetadataCreationService)
                .createUploadingMetadata(any(FileUploadCommand.class));

        doThrow(new CannotCreateTransactionException(
                "Simulated transient database failure"
        ))
                .when(fileUploadStateManager)
                .markCompleted(anyLong(), anyString());

        assertThrows(
                InternalServerException.class,
                () -> fileUploadService.processUploadFile(
                        new UploadRequestObjectDTO(
                                MockObjectBuilder.getTextContentTypeMultipartFile()
                        )
                )
        );

        FileMetadata metadata = fileMetadataRepository
                .findById(metadataId.get())
                .orElseThrow();

        assertEquals(FileState.FAILED, metadata.getStatus());
        assertThrows(RuntimeException.class, () -> getUploadedFile(metadata.getObjectKey()));
    }
}