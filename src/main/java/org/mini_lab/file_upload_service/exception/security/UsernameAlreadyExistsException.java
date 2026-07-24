package org.mini_lab.file_upload_service.exception.security;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("User %s already exist", username));
    }
}
