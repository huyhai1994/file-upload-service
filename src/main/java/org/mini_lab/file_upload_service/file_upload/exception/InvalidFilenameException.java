package org.mini_lab.file_upload_service.file_upload.exception;

public class InvalidFilenameException extends RuntimeException {
    public InvalidFilenameException() {
        super("File name not valid");
    }
}
