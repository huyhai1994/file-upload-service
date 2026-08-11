package org.mini_lab.file_upload_service.file_upload.shared.exception;

public class InvalidFileExtensionException extends RuntimeException{
    public InvalidFileExtensionException(){
        super("File extension not valid");
    }
}
