package org.mini_lab.file_upload_service.exception.file_upload;

public class InvalidFileExtensionException extends RuntimeException{
    public InvalidFileExtensionException(){
        super("File extension not valid");
    }
}
