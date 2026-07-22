package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.FileDeleteResponseDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

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
        objectStorageClient.delete(fileMetadata.getObjectKey());
        fileMetadataStateManager.markDeleted(fileId);

        return new FileDeleteResponseDTO(true);
    }

}
