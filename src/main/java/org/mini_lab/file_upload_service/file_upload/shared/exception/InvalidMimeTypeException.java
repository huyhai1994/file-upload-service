package org.mini_lab.file_upload_service.file_upload.shared.exception;


public class InvalidMimeTypeException extends RuntimeException {
    public InvalidMimeTypeException() {
        super("Mime type  not valid");
    }
}
