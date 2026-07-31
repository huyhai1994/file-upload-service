package org.mini_lab.file_upload_service.file_upload.service.retry;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.configuration.FileDeleteRecoveryProperties;
import org.mini_lab.file_upload_service.file_upload.entity.FileState;
import org.mini_lab.file_upload_service.file_upload.repository.FileMetadataRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TimedOutDeletingFileFinder implements TimedOutFileFinder {

    private final Clock clock;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileDeleteRecoveryProperties fileDeleteRecoveryProperties;

    @Override
    public List<Long> findTimedOutFileIds() {
        return fileMetadataRepository
                .findTimedOutFiledIds(
                        LocalDateTime.now(clock).minusMinutes(fileDeleteRecoveryProperties.getDeletingTimeout().toMinutes()),
                        FileState.DELETING,
                        PageRequest.of(0, fileDeleteRecoveryProperties.getBatchSize()));
    }


}
