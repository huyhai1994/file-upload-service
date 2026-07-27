package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.exception.security.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.exception.security.UsernameLengthExceededException;
import org.mini_lab.file_upload_service.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameVerifyServiceMockTest {

    @InjectMocks
    private UsernameVerifyService usernameVerifyService;

    @Mock
    private UserRepository userRepository;

    @Test
    void verify_whenUsernameLengthIs100_thenThrowUsernameLengthExceededException() {
        String username = "a".repeat(100);

        assertThrows(
                UsernameLengthExceededException.class,
                () -> usernameVerifyService.verify(username)
        );

        verify(userRepository, never()).existsByUsername(username);
    }

    @Test
    void verify_whenUsernameLengthIs99AndUsernameDoesNotExist_thenDoesNotThrow() {
        String username = "a".repeat(99);

        when(userRepository.existsByUsername(username)).thenReturn(false);

        assertDoesNotThrow(() -> usernameVerifyService.verify(username));

        verify(userRepository).existsByUsername(username);
    }

    @Test
    void verify_wheUsernameLengthIs99AndUsernameIsExist_whenThrowUsernameAlreadyExistsException() {
        String username = "a".repeat(99);
        when(userRepository.existsByUsername(username)).thenReturn(true);
        assertThrows(UsernameAlreadyExistsException.class, () -> usernameVerifyService.verify(username));

    }
}