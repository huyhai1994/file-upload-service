package org.mini_lab.file_upload_service.exception.file_upload;

public class InvalidFilenameException extends RuntimeException {
    public InvalidFilenameException() {
        super("File name not valid");
    }
}
