package org.mini_lab.file_upload_service.exception;

public class InternalServerException extends RuntimeException{
    public InternalServerException(){
        super("Internal Error");
    }
}
