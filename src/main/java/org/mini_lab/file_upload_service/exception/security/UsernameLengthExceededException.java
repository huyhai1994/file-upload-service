package org.mini_lab.file_upload_service.exception.security;

public class UsernameLengthExceededException extends RuntimeException {
    public UsernameLengthExceededException(int length) {
        super(String.format("Username must not exceed %d characters", length));
    }
}
