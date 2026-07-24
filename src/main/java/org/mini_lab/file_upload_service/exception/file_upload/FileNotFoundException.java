package org.mini_lab.file_upload_service.exception.file_upload;

import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND.getDefaultMessage());
    }

}
