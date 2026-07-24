package org.mini_lab.file_upload_service.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.service.retry.FileDeleteRetryService;
import org.mini_lab.file_upload_service.service.retry.TimedOutFileFinder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDeleteRetryScheduler {

    private final TimedOutFileFinder timedOutFileFinder;
    private final FileDeleteRetryService fileDeleteRetryService;

    @Scheduled(
            fixedDelayString = "${file.file-delete-recovery.scheduler-interval}",
            timeUnit = TimeUnit.MINUTES
    )
    public void retryTimedOutDeletingFiles() {
        List<Long> fileIds = timedOutFileFinder.findTimedOutFileIds();

        for (Long fileId : fileIds) {
            try {
                fileDeleteRetryService.retryTimedOutFile(fileId);
            } catch (Exception ex) {
                log.error(
                        "RETRY_TIMED_OUT_DELETING_FILE_FAILED fileId={}",
                        fileId,
                        ex
                );
            }
        }
    }
}