package org.mini_lab.file_upload_service.service.retry;

import java.util.List;

public interface TimedOutFileFinder {
    List<Long> findTimedOutFileIds();
}
