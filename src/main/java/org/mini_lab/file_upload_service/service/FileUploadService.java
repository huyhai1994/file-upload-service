package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.mini_lab.file_upload_service.dto.FileMetadataResponseDTO;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileUploadRequestExtractor fileUploadRequestExtractor;
    private final FileRequestVerifyService fileRequestVerifyService;
    private final FileMetadataCreationService fileMetadataCreationService;
    private final FileUploadStateManager fileUploadStateManager;
    private final ObjectStorageClient objectStorageClient;

    public FileMetadataResponseDTO processUploadFile(
            UploadRequestObjectDTO request
    ) throws FileUploadException {
        FileUploadCommand command = fileUploadRequestExtractor.extract(request);

        fileRequestVerifyService.validate(command);

        FileMetadata metadata = fileMetadataCreationService.createUploadingMetadata(command);

        try {
            UploadObjectResult uploadResult = objectStorageClient.upload(metadata.getObjectKey(), command);

            fileUploadStateManager.markCompleted(metadata.getId(), uploadResult.checksum());

            return buildCompletedResponse(metadata, uploadResult.checksum());

        } catch (Exception uploadProcessException) {
            handleUploadFailure(metadata, uploadProcessException);

            throw new FileUploadException(
                    "Failed to upload file: " + metadata.getFileName(),
                    uploadProcessException
            );
        }
    }

    private void handleUploadFailure(
            FileMetadata metadata,
            Exception originalException
    ) {
        try {
            objectStorageClient.delete(metadata.getObjectKey());
        } catch (Exception compensationException) {
            originalException.addSuppressed(compensationException);
        }

        try {
            fileUploadStateManager.markFailed(metadata.getId());
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
                .checksum(checksum)
                .state(FileState.COMPLETED)
                .build();
    }
}

