package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterResponse;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.register.exception.PasswordLengthExceededException;
import org.mini_lab.file_upload_service.security.authentication.register.exception.PasswordTooShortException;
import org.mini_lab.file_upload_service.security.authentication.register.exception.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.security.authentication.register.exception.UsernameLengthExceededException;
import org.mini_lab.file_upload_service.security.authentication.register.service.UserAccountRegisterService;
import org.mini_lab.file_upload_service.security.authentication.register.service.UsernameVerifyService;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.mini_lab.file_upload_service.security.authentication.shared.service.PasswordVerifyService;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.PASSWORD_HASH;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.VALID_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.DEFAULT_USERNAME;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountRegisterServiceMockTest {

    @InjectMocks
    private UserAccountRegisterService userAccountRegisterService;

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
        String rawUsername = "A".repeat(100);
        String normalizedUsername = "a".repeat(100);

        RegisterRequest request = registerRequest(rawUsername, VALID_PASSWORD);

        mockNormalizedUsername(request, normalizedUsername);

        doThrow(new UsernameLengthExceededException(4, 50))
                .when(usernameVerifyService)
                .verify(normalizedUsername);

        assertThrows(
                UsernameLengthExceededException.class,
                () -> userAccountRegisterService.register(request)
        );

        verifyRegistrationStoppedBeforePasswordValidation();
    }

    @Test
    void register_whenPasswordTooShort_thenThrowExceptionAndNotSave() {
        String password = "1234567";
        RegisterRequest request = registerRequestWithPassword(password);

        mockNormalizedUsername(request);

        doThrow(new PasswordTooShortException(8))
                .when(passwordVerifyService)
                .verify(password);

        assertThrows(
                PasswordTooShortException.class,
                () -> userAccountRegisterService.register(request)
        );

        verify(usernameVerifyService).verify(NORMALIZED_USERNAME);
        verifyRegistrationStoppedBeforeEncoding();
    }

    @Test
    void register_whenPasswordTooLong_thenThrowExceptionAndNotSave() {
        String password = "a".repeat(73);
        RegisterRequest request = registerRequestWithPassword(password);

        mockNormalizedUsername(request);

        doThrow(new PasswordLengthExceededException(72))
                .when(passwordVerifyService)
                .verify(password);

        assertThrows(
                PasswordLengthExceededException.class,
                () -> userAccountRegisterService.register(request)
        );

        verify(usernameVerifyService).verify(NORMALIZED_USERNAME);
        verifyRegistrationStoppedBeforeEncoding();
    }

    @Test
    void register_whenUserExists_thenThrowExceptionAndNotSave() {
        RegisterRequest request = validRegisterRequest();

        mockNormalizedUsername(request);

        doThrow(new UsernameAlreadyExistsException(NORMALIZED_USERNAME))
                .when(usernameVerifyService)
                .verify(NORMALIZED_USERNAME);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userAccountRegisterService.register(request)
        );

        verifyRegistrationStoppedBeforePasswordValidation();
    }

    @Test
    void register_whenRequestIsValid_thenEncodePasswordAndSaveUser() {
        RegisterRequest request = validRegisterRequest();
        UUID userId = UUID.randomUUID();

        mockSuccessfulRegistration(request, userId);

        RegisterResponse response =
                userAccountRegisterService.register(request);

        User savedUser = captureSavedUser();

        assertEquals(NORMALIZED_USERNAME, savedUser.getUsername());
        assertEquals(PASSWORD_HASH, savedUser.getPasswordHash());
        assertNotEquals(VALID_PASSWORD, savedUser.getPasswordHash());

        assertEquals(userId, response.id());
        assertEquals(NORMALIZED_USERNAME, response.username());

        verifyRegistrationOrder(request);
    }

    @Test
    void register_whenDatabaseUniqueConstraintIsViolated_thenThrowUsernameAlreadyExistsException() {
        RegisterRequest request = validRegisterRequest();

        mockNormalizedUsername(request);
        mockPasswordEncoding(request);

        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate username"
                ));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userAccountRegisterService.register(request)
        );

        verifyCompleteRegistrationAttempt(request);
    }

    private RegisterRequest validRegisterRequest() {
        return registerRequest(DEFAULT_USERNAME, VALID_PASSWORD);
    }

    private RegisterRequest registerRequestWithPassword(String password) {
        return registerRequest(DEFAULT_USERNAME, password);
    }

    private RegisterRequest registerRequest(String username, String password) {
        return new RegisterRequest(username, password);
    }


    private void mockNormalizedUsername(RegisterRequest request) {
        mockNormalizedUsername(request, NORMALIZED_USERNAME);
    }

    private void mockNormalizedUsername(
            RegisterRequest request,
            String normalizedUsername
    ) {
        when(normalizeUsernameService.normalizeUsername(request.username()))
                .thenReturn(normalizedUsername);
    }

    private void mockPasswordEncoding(RegisterRequest request) {
        when(passwordEncoder.encode(request.password()))
                .thenReturn(PASSWORD_HASH);
    }

    private void mockSuccessfulRegistration(
            RegisterRequest request,
            UUID userId
    ) {
        mockNormalizedUsername(request);
        mockPasswordEncoding(request);

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(userId);
                    return user;
                });
    }

    private User captureSavedUser() {
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).saveAndFlush(captor.capture());

        return captor.getValue();
    }

    private void verifyRegistrationStoppedBeforePasswordValidation() {
        verify(passwordVerifyService, never()).verify(anyString());
        verifyRegistrationStoppedBeforeEncoding();
    }

    private void verifyRegistrationStoppedBeforeEncoding() {
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).saveAndFlush(any());
    }

    private void verifyCompleteRegistrationAttempt(
            RegisterRequest request
    ) {
        verify(usernameVerifyService).verify(NORMALIZED_USERNAME);
        verify(passwordVerifyService).verify(request.password());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    private void verifyRegistrationOrder(RegisterRequest request) {
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
                .verify(NORMALIZED_USERNAME);

        inOrder.verify(passwordVerifyService)
                .verify(request.password());

        inOrder.verify(passwordEncoder)
                .encode(request.password());

        inOrder.verify(userRepository)
                .saveAndFlush(any(User.class));
    }

}