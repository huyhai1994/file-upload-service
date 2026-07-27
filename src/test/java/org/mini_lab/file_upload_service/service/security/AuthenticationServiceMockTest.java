package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.dto.security.RegisterRequest;
import org.mini_lab.file_upload_service.dto.security.RegisterResponse;
import org.mini_lab.file_upload_service.entity.User;
import org.mini_lab.file_upload_service.exception.security.PasswordLengthExceededException;
import org.mini_lab.file_upload_service.exception.security.PasswordTooShortException;
import org.mini_lab.file_upload_service.exception.security.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.exception.security.UsernameLengthExceededException;
import org.mini_lab.file_upload_service.repository.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceMockTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NormalizeUsernameService normalizeUsernameService;

    @Mock
    private UsernameVerifyService usernameVerifyService;

    @Mock
    private PasswordVerifyService passwordVerifyService;

    @Test
    void register_whenUsernameTooLong_thenThrowExceptionAndNotSave() {
        RegisterRequest request =
                new RegisterRequest(" A".repeat(100), "password123");

        String normalizedUsername = "a".repeat(100);

        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);

        doThrow(new UsernameLengthExceededException(100))
                .when(usernameVerifyService)
                .verify(normalizedUsername);

        assertThrows(
                UsernameLengthExceededException.class,
                () -> authenticationService.register(request)
        );

        verify(passwordVerifyService, never()).verify(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenPasswordTooShort_thenThrowExceptionAndNotSave() {
        RegisterRequest request =
                new RegisterRequest(" Hai ", "1234567");

        String normalizedUsername = "hai";

        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);

        doThrow(new PasswordTooShortException(8))
                .when(passwordVerifyService)
                .verify(request.password());

        assertThrows(
                PasswordTooShortException.class,
                () -> authenticationService.register(request)
        );

        verify(usernameVerifyService).verify(normalizedUsername);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenPasswordTooLong_thenThrowExceptionAndNotSave() {
        String password = "a".repeat(73);

        RegisterRequest request =
                new RegisterRequest(" Hai ", password);

        String normalizedUsername = "hai";

        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);

        doThrow(new PasswordLengthExceededException(72))
                .when(passwordVerifyService)
                .verify(password);

        assertThrows(
                PasswordLengthExceededException.class,
                () -> authenticationService.register(request)
        );

        verify(usernameVerifyService).verify(normalizedUsername);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenUserExists_thenThrowExceptionAndNotSave() {
        RegisterRequest request =
                new RegisterRequest(" Hai ", "password123");

        String normalizedUsername = "hai";

        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);

        doThrow(new UsernameAlreadyExistsException(normalizedUsername))
                .when(usernameVerifyService)
                .verify(normalizedUsername);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> authenticationService.register(request)
        );

        verify(passwordVerifyService, never()).verify(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenRequestIsValid_thenEncodePasswordAndSaveUser() {
        RegisterRequest request =
                new RegisterRequest(" Hai ", "password123");

        String normalizedUsername = "hai";
        String passwordHash = "$2a$10$encoded-password";
        UUID userId = UUID.randomUUID();

        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);

        when(passwordEncoder.encode(request.password()))
                .thenReturn(passwordHash);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(userId);
                    return user;
                });

        RegisterResponse response =
                authenticationService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(normalizedUsername, savedUser.getUsername());
        assertEquals(passwordHash, savedUser.getPasswordHash());
        assertNotEquals(request.password(), savedUser.getPasswordHash());

        assertEquals(userId, response.id());
        assertEquals(normalizedUsername, response.username());

        InOrder inOrder = inOrder(
                normalizeUsernameService,
                usernameVerifyService,
                passwordVerifyService,
                passwordEncoder,
                userRepository
        );

        inOrder.verify(normalizeUsernameService)
                .normalizeUsername(request.username());

        inOrder.verify(usernameVerifyService)
                .verify(normalizedUsername);

        inOrder.verify(passwordVerifyService)
                .verify(request.password());

        inOrder.verify(passwordEncoder)
                .encode(request.password());

        inOrder.verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_whenDatabaseUniqueConstraintIsViolated_thenThrowUsernameAlreadyExistsException() {
        RegisterRequest request =
                new RegisterRequest(" Hai ", "password123");

        String normalizedUsername = "hai";
        String passwordHash = "$2a$10$encoded-password";

        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);

        when(passwordEncoder.encode(request.password()))
                .thenReturn(passwordHash);

        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate username"
                ));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> authenticationService.register(request)
        );

        verify(usernameVerifyService).verify(normalizedUsername);
        verify(passwordVerifyService).verify(request.password());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }
}