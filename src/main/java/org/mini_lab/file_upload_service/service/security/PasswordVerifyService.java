package org.mini_lab.file_upload_service.service.security;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.exception.security.PasswordLengthExceededException;
import org.mini_lab.file_upload_service.exception.security.PasswordTooShortException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordVerifyService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72;

    public void verify(String password) {
        validateMinLength(password);
        validateMaxLength(password);
    }

    private void validateMinLength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new PasswordTooShortException(MIN_PASSWORD_LENGTH);
        }
    }

    private void validateMaxLength(String password) {
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new PasswordLengthExceededException(MAX_PASSWORD_LENGTH);
        }
    }
}