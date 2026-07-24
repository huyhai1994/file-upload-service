package org.mini_lab.file_upload_service.exception.security;

public class PasswordLengthExceededException extends RuntimeException {
    public PasswordLengthExceededException(int maxPasswordLength) {
        super("Password must not exceed " + maxPasswordLength + " characters.");

    }
}
