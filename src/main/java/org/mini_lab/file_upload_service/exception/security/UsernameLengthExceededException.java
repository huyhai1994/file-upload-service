package org.mini_lab.file_upload_service.exception.security;

public class UsernameLengthExceededException extends RuntimeException {
    public UsernameLengthExceededException(int lower, int upper) {
        super(String.format("Username must not exceed %d - %d characters", lower, upper));
    }
}
