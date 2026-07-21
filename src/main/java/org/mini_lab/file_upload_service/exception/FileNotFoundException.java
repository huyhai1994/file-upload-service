package org.mini_lab.file_upload_service.exception;

import org.mini_lab.file_upload_service.enums.ErrorCode;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND.getDefaultMessage());
    }

}
