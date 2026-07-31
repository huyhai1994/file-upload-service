package org.mini_lab.file_upload_service.file_upload.service.delete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.file_upload.dto.file_upload.FileDeleteResponseDTO;
import org.mini_lab.file_upload_service.file_upload.entity.FileMetadata;
import org.mini_lab.file_upload_service.file_upload.entity.FileState;
import org.mini_lab.file_upload_service.file_upload.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.file_upload.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.file_upload.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.file_upload.service.state_manager.FileMetadataStateManager;
import org.mini_lab.file_upload_service.file_upload.service.validator.FileVerifyService;
import org.mini_lab.file_upload_service.file_upload.service.s3.ObjectStorageClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeleteService {

    private final FileMetadataStateManager fileMetadataStateManager;
    private final ObjectStorageClient objectStorageClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileVerifyService fileVerifyService;

    public FileDeleteResponseDTO processDeleteFile(Long fileId) {

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId).orElseThrow(FileNotFoundException::new);
        fileVerifyService.verifyFileAvailable(fileId, FileState.COMPLETED, fileMetadata.getStatus());
        fileMetadataStateManager.markDeleting(fileId);
        try {
            objectStorageClient.delete(fileMetadata.getObjectKey());
        } catch (ObjectStorageException exception) {
            log.error("DELETE_OBJECT_FAILED fileId={}", fileId);
            throw exception;
        }
        fileMetadataStateManager.markDeleted(fileId);

        return new FileDeleteResponseDTO(true);
    }

}
