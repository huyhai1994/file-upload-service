package org.mini_lab.file_upload_service.exception.file_upload;

import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;

public class InternalServerException extends RuntimeException{
    public InternalServerException(){
        super(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }
}
