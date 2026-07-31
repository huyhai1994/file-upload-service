package org.mini_lab.file_upload_service.security.authentication.exception;

public class PasswordLengthExceededException extends RuntimeException {
    public PasswordLengthExceededException(int maxPasswordLength) {
        super("Password must not exceed " + maxPasswordLength + " characters.");

    }
}
