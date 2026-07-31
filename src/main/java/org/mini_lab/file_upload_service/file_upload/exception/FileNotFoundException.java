package org.mini_lab.file_upload_service.file_upload.exception;

import org.mini_lab.file_upload_service.file_upload.enums.ErrorCode;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND.getDefaultMessage());
    }

}
