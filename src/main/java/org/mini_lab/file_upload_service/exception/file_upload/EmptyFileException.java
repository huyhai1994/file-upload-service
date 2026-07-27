package org.mini_lab.file_upload_service.exception.file_upload;

import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super(ErrorCode.EMPTY_FILE.getDefaultMessage());
    }
}
