package org.mini_lab.file_upload_service.exception.file_upload;


public class InvalidMimeTypeException extends RuntimeException {
    public InvalidMimeTypeException() {
        super("Mime type  not valid");
    }
}
