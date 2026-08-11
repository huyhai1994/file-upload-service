package org.mini_lab.file_upload_service.file_upload.upload.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransactionException;
import org.mini_lab.file_upload_service.file_upload.upload.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.file_upload.upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.upload.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.file_upload.upload.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.file_upload.shared.entity.FileMetadata;
import org.mini_lab.file_upload_service.file_upload.shared.entity.FileState;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InternalServerException;
import org.mini_lab.file_upload_service.file_upload.shared.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.file_upload.upload.service.state_manager.FileMetadataStateManager;
import org.mini_lab.file_upload_service.file_upload.upload.service.validator.FileVerifyService;
import org.mini_lab.file_upload_service.file_upload.shared.s3.ObjectStorageClient;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileUploadRequestExtractor fileUploadRequestExtractor;
    private final FileVerifyService fileVerifyService;
    private final FileMetadataCreationService fileMetadataCreationService;
    private final FileMetadataStateManager fileMetadataStateManager;
    private final ObjectStorageClient objectStorageClient;

    @WithSpan("process-upload-file")
    public FileMetadataResponseDTO processUploadFile(
            UploadRequestObjectDTO request
    ) {
        log.info("PROCESS_UPLOAD_FILE");
        FileUploadCommand command =
                fileUploadRequestExtractor.extract(request);

        fileVerifyService.validate(command);

        FileMetadata metadata;

        try {
            log.info("CREATE_UPLOADING_METADATA");
            metadata = fileMetadataCreationService.createUploadingMetadata(command);
        } catch (DataAccessException | TransactionException | CannotCreateTransactionException exception) {
            throw new InternalServerException();
        }

        UploadObjectResult uploadResult;

        try {
            log.info("UPLOADING_TO_OBJECTSTORAGE");
            uploadResult = objectStorageClient.upload(
                    metadata.getObjectKey(),
                    command
            );
        } catch (ObjectStorageException exception) {
            handleUploadFailure(metadata, exception);
            throw new InternalServerException();
        }

        try {
            log.info("METADATA_STATE_CHANGED to COMPLETED");
            fileMetadataStateManager.markCompleted(
                    metadata.getId(),
                    uploadResult.checksum()
            );
        } catch (DataAccessException | TransactionException | CannotCreateTransactionException exception) {
            handleUploadFailure(metadata, exception);
            throw new InternalServerException();
        }

        return buildCompletedResponse(
                metadata,
                uploadResult.checksum()
        );
    }

    public void handleUploadFailure(
            FileMetadata metadata,
            Exception originalException
    ) {
        try {
            log.info("DELETING_OBJECT_IN_OBJECTSTORAGE");
            objectStorageClient.delete(metadata.getObjectKey());
        } catch (ObjectStorageException compensationException) {
            originalException.addSuppressed(compensationException);
        }

        try {
            log.info("METADATA_STATE_CHANGED to FAILED");
            fileMetadataStateManager.markFailed(metadata.getId());
        } catch (Exception stateUpdateException) {
            originalException.addSuppressed(stateUpdateException);
        }
    }

    public FileMetadataResponseDTO buildCompletedResponse(
            FileMetadata metadata,
            String checksum
    ) {
        return FileMetadataResponseDTO.builder()
                .fileId(metadata.getId())
                .fileName(metadata.getFileName())
                .title(metadata.getTitle())
                .contentType(metadata.getContentType())
                .extension(metadata.getExtension())
                .size(metadata.getSize())
                .checksum(checksum)
                .state(FileState.COMPLETED)
                .createdAt(metadata.getCreatedAt())
                .build();
    }
}

