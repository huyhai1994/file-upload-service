package org.mini_lab.file_upload_service.file_upload.shared.exception;

import org.mini_lab.file_upload_service.shared.error_code.ErrorCode;

public class InternalServerException extends RuntimeException{
    public InternalServerException(){
        super(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }
}
