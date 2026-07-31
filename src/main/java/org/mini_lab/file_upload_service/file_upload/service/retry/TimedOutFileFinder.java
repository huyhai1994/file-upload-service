package org.mini_lab.file_upload_service.file_upload.service.retry;

import java.util.List;

public interface TimedOutFileFinder {
    List<Long> findTimedOutFileIds();
}
