package org.mini_lab.file_upload_service.security.exception;

public class PasswordTooShortException extends RuntimeException {
    public PasswordTooShortException(int minPasswordLength) {
        super("Password must contain at least " + minPasswordLength + " characters.");

    }
}
