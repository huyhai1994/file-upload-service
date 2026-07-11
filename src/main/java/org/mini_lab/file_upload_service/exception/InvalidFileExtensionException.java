package org.mini_lab.file_upload_service.exception;

public class InvalidFileExtensionException extends RuntimeException{
    public InvalidFileExtensionException(){
        super("File extension not valid");
    }
}
