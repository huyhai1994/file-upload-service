package org.mini_lab.file_upload_service.service.state_manager;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileMetadataStateManager {
    private final FileMetadataRepository fileMetadataRepository;
    private final Clock clock;

    @Transactional
    public void markCompleted(Long fileId, String checksum) {
        int affectedRowCount = fileMetadataRepository.markCompletedIfUploading(fileId, checksum, LocalDateTime.now(clock));
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    @Transactional
    public void markFailed(Long fileId) {
        int affectedRowCount = fileMetadataRepository.markFailedIfUploading(fileId, LocalDateTime.now(clock));
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    @Transactional
    public void markDeleting(Long fileId) {
        int affectedRowCount = fileMetadataRepository.markDeletingIfCompleted(fileId, LocalDateTime.now(clock));
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    @Transactional
    public void markDeleted(Long fileId) {
        int affectedRowCount = fileMetadataRepository.markDeletedIfDeleting(fileId, LocalDateTime.now(clock));
        assertOnlyOneAffectedRow(affectedRowCount);
    }

    private void assertOnlyOneAffectedRow(int affectedRowCount) {
        if (affectedRowCount != 1) {
            throw new InvalidStateTransitionException();
        }
    }
}
