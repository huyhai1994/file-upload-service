package org.mini_lab.file_upload_service.file_upload.service.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.file_upload.entity.FileMetadata;
import org.mini_lab.file_upload_service.file_upload.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.file_upload.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.file_upload.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.file_upload.service.s3.ObjectStorageClient;
import org.mini_lab.file_upload_service.file_upload.service.state_manager.FileMetadataStateManager;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileDeleteRetryService {
    private final ObjectStorageClient objectStorageClient;
    private final FileMetadataStateManager fileMetadataStateManager;
    private final FileMetadataRepository fileMetadataRepository;

    public void retryTimedOutFile(Long fileId) {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findById(fileId).orElseThrow(FileNotFoundException::new);
            objectStorageClient.delete(fileMetadata.getObjectKey());
            fileMetadataStateManager.markDeleted(fileId);
        } catch (ObjectStorageException ex) {
            log.error("RETRY_TIMEDOUT_FILE fileId={} error={}", fileId, ex.getMessage());
        }
    }
}
