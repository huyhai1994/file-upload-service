package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.exception.security.PasswordLengthExceededException;
import org.mini_lab.file_upload_service.exception.security.PasswordTooShortException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordVerifyServiceTest {

    private final PasswordVerifyService passwordVerifyService =
            new PasswordVerifyService(new BCryptPasswordEncoder());

    @Test
    void verify_whenPasswordLengthIs7_thenThrowPasswordTooShortException() {
        String password = "a".repeat(7);

        assertThrows(
                PasswordTooShortException.class,
                () -> passwordVerifyService.verify(password)
        );
    }

    @Test
    void verify_whenPasswordLengthIs8_thenDoesNotThrow() {
        String password = "a".repeat(8);

        assertDoesNotThrow(
                () -> passwordVerifyService.verify(password)
        );
    }

    @Test
    void verify_whenPasswordLengthIs72_thenDoesNotThrow() {
        String password = "a".repeat(72);

        assertDoesNotThrow(
                () -> passwordVerifyService.verify(password)
        );
    }

    @Test
    void verify_whenPasswordLengthIs73_thenThrowPasswordLengthExceededException() {
        String password = "a".repeat(73);

        assertThrows(
                PasswordLengthExceededException.class,
                () -> passwordVerifyService.verify(password)
        );
    }
}