package org.mini_lab.file_upload_service.exception;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super("File Empty");
    }
}
