package org.mini_lab.file_upload_service.security.authentication.register.exception;

public class UsernameLengthExceededException extends RuntimeException {
    public UsernameLengthExceededException(int lower, int upper) {
        super(String.format("Username must not exceed %d - %d characters", lower, upper));
    }
}
