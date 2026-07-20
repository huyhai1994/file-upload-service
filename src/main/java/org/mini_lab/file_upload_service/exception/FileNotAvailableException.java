package org.mini_lab.file_upload_service.exception;

import org.mini_lab.file_upload_service.entity.FileState;

public class FileNotAvailableException extends RuntimeException {
    public FileNotAvailableException(Long fileId, FileState state) {
        super(String.format("File with %d id and state %s not available", fileId, state));
    }
}
