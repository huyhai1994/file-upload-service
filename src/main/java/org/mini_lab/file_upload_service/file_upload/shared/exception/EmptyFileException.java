package org.mini_lab.file_upload_service.file_upload.shared.exception;

import org.mini_lab.file_upload_service.file_upload.enums.ErrorCode;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super(ErrorCode.EMPTY_FILE.getDefaultMessage());
    }
}
