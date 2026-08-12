package org.mini_lab.file_upload_service.file_upload.shared.exception;

import org.mini_lab.file_upload_service.shared.error_code.ErrorCode;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND.getDefaultMessage());
    }

}
