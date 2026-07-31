package org.mini_lab.file_upload_service.security.authentication.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("User %s already exist", username));
    }
}
