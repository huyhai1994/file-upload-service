package org.mini_lab.file_upload_service.security.authentication.register.exception;

public class PasswordLengthExceededException extends RuntimeException {
    public PasswordLengthExceededException(int maxPasswordLength) {
        super("Password must not exceed " + maxPasswordLength + " characters.");

    }
}
