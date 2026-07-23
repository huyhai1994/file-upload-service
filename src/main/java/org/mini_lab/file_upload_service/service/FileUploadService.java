package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.TransactionException;
import org.mini_lab.file_upload_service.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.InternalServerException;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileUploadRequestExtractor fileUploadRequestExtractor;
    private final FileVerifyService fileVerifyService;
    private final FileMetadataCreationService fileMetadataCreationService;
    private final FileMetadataStateManager fileMetadataStateManager;
    private final ObjectStorageClient objectStorageClient;

    public FileMetadataResponseDTO processUploadFile(
            UploadRequestObjectDTO request
    ) {
        FileUploadCommand command =
                fileUploadRequestExtractor.extract(request);

        fileVerifyService.validate(command);

        FileMetadata metadata;

        try {
            metadata =
                    fileMetadataCreationService.createUploadingMetadata(command);
        } catch (DataAccessException | TransactionException | CannotCreateTransactionException exception) {
            throw new InternalServerException();
        }

        UploadObjectResult uploadResult;

        try {
            uploadResult = objectStorageClient.upload(
                    metadata.getObjectKey(),
                    command
            );
        } catch (ObjectStorageException exception) {
            handleUploadFailure(metadata, exception);
            throw new InternalServerException();
        }

        try {
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
            objectStorageClient.delete(metadata.getObjectKey());
        } catch (ObjectStorageException compensationException) {
            originalException.addSuppressed(compensationException);
        }

        try {
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

