package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileMetadataStateManager {
    private final FileMetadataRepository fileMetadataRepository;

    @Transactional
    public void markCompleted(Long fileId, String checksum) {
        int affectedRowCount = fileMetadataRepository.markCompletedIfUploading(fileId, checksum);
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    @Transactional
    public void markFailed(Long fileId) {
        int affectedRowCount = fileMetadataRepository.markFailedIfUploading(fileId);
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    @Transactional
    public void markDeleting(Long fileId) {
        int affectedRowCount = fileMetadataRepository.markDeletingIfCompleted(fileId);
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    @Transactional
    public void markDeleted(Long fileId) {
        int affectedRowCount = fileMetadataRepository.markDeletedIfDeleting(fileId);
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    private void assertOnlyOneAffectedRow(int affectedRowCount) {
        if (affectedRowCount != 1) {
            throw new InvalidStateTransitionException();
        }
    }
}
