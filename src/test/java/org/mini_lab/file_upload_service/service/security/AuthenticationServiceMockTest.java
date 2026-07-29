package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.dto.security.LoginRequest;
import org.mini_lab.file_upload_service.dto.security.LoginResponse;
import org.mini_lab.file_upload_service.dto.security.RegisterRequest;
import org.mini_lab.file_upload_service.dto.security.RegisterResponse;
import org.mini_lab.file_upload_service.entity.User;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockAccessTokenBuilder.ACCESS_TOKEN;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.PASSWORD_HASH;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.VALID_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.DEFAULT_USERNAME;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtService jwtService;


    @Mock
    private UserDetails userDetails;

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
                () -> authenticationService.register(request)
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
                () -> authenticationService.register(request)
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
                () -> authenticationService.register(request)
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
                () -> authenticationService.register(request)
        );

        verifyRegistrationStoppedBeforePasswordValidation();
    }

    @Test
    void register_whenRequestIsValid_thenEncodePasswordAndSaveUser() {
        RegisterRequest request = validRegisterRequest();
        UUID userId = UUID.randomUUID();

        mockSuccessfulRegistration(request, userId);

        RegisterResponse response =
                authenticationService.register(request);

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
                () -> authenticationService.register(request)
        );

        verifyCompleteRegistrationAttempt(request);
    }

    @Test
    void login_whenLoginRequestValid_thenAuthenticateAndReturnLoginResponse() {
        when(normalizeUsernameService.normalizeUsername(DEFAULT_USERNAME)).thenReturn(NORMALIZED_USERNAME);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn(ACCESS_TOKEN);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        LoginRequest loginRequest = validLoginRequest();

        LoginResponse loginResponse = authenticationService.login(loginRequest);

        assertNotNull(loginResponse);
        assertEquals(ACCESS_TOKEN, loginResponse.accessToken());

        verifyLoginOrder(loginRequest);

    }

    @Test
    void login_whenBadCredentials_thenThrowBadCredentialsExceptionAndNotGenerateToken() {
        when(normalizeUsernameService.normalizeUsername(DEFAULT_USERNAME))
                .thenReturn(NORMALIZED_USERNAME);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException(
                ErrorCode.BAD_CREDENTIAL.getDefaultMessage()
        ));

        LoginRequest loginRequest = validLoginRequest();

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login(loginRequest)
        );

        assertEquals(
                ErrorCode.BAD_CREDENTIAL.getDefaultMessage(),
                exception.getMessage()
        );

        verify(normalizeUsernameService)
                .normalizeUsername(loginRequest.username());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verifyNoInteractions(jwtService);
    }
    private RegisterRequest validRegisterRequest() {
        return registerRequest(DEFAULT_USERNAME, VALID_PASSWORD);
    }

    private LoginRequest validLoginRequest() {
        return loginRequest(DEFAULT_USERNAME, VALID_PASSWORD);
    }

    private RegisterRequest registerRequestWithPassword(String password) {
        return registerRequest(DEFAULT_USERNAME, password);
    }

    private RegisterRequest registerRequest(String username, String password) {
        return new RegisterRequest(username, password);
    }

    private LoginRequest loginRequest(String username, String password) {
        return new LoginRequest(username, password);
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

    private void verifyLoginOrder(LoginRequest request) {
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        InOrder inOrder = inOrder(
                normalizeUsernameService,
                authenticationManager,
                authentication,
                jwtService
        );

        inOrder.verify(normalizeUsernameService)
                .normalizeUsername(request.username());

        inOrder.verify(authenticationManager)
                .authenticate(tokenCaptor.capture());

        inOrder.verify(authentication)
                .getPrincipal();

        inOrder.verify(jwtService)
                .generateAccessToken(userDetails);

        UsernamePasswordAuthenticationToken capturedToken =
                tokenCaptor.getValue();

        assertEquals(
                NORMALIZED_USERNAME,
                capturedToken.getPrincipal()
        );

        assertEquals(
                request.password(),
                capturedToken.getCredentials()
        );

        assertFalse(capturedToken.isAuthenticated());
    }
}