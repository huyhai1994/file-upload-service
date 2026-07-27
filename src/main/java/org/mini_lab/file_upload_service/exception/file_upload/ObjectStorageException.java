package org.mini_lab.file_upload_service.exception.file_upload;

public class ObjectStorageException extends RuntimeException {
    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectStorageException() {
    }
}
