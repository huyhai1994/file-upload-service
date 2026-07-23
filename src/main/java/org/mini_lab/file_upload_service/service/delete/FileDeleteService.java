package org.mini_lab.file_upload_service.service.delete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.dto.FileDeleteResponseDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.state_manager.FileMetadataStateManager;
import org.mini_lab.file_upload_service.service.validator.FileVerifyService;
import org.mini_lab.file_upload_service.service.s3.ObjectStorageClient;
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
