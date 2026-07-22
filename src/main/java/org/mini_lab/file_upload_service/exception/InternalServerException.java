package org.mini_lab.file_upload_service.exception;

import org.mini_lab.file_upload_service.enums.ErrorCode;

public class InternalServerException extends RuntimeException{
    public InternalServerException(){
        super(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }
}
