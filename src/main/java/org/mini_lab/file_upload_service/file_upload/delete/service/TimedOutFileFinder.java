package org.mini_lab.file_upload_service.file_upload.delete.service;

import java.util.List;

public interface TimedOutFileFinder {
    List<Long> findTimedOutFileIds();
}
