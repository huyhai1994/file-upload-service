package org.mini_lab.file_upload_service.file_upload.shared.exception;

public class ObjectStorageException extends RuntimeException {
    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectStorageException() {
    }
}
