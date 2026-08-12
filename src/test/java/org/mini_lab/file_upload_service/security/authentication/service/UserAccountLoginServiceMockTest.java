package org.mini_lab.file_upload_service.security.authentication.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.shared.error_code.ErrorCode;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginRequest;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.authentication.login.service.LoginAttemptService;
import org.mini_lab.file_upload_service.security.authentication.login.service.UserAccountLoginService;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.mini_lab.file_upload_service.security.jwt.service.JwtAccessTokenService;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockAccessTokenBuilder.ACCESS_TOKEN;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.VALID_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.DEFAULT_USERNAME;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountLoginServiceMockTest {

    @InjectMocks
    UserAccountLoginService userAccountLoginService;

    @Mock
    LoginAttemptService loginAttemptService;

    @Mock
    private NormalizeUsernameService normalizeUsernameService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtAccessTokenService jwtAccessTokenService;

    @Mock
    private UserDetails userDetails;

    @Test
    void login_whenLoginRequestValid_thenAuthenticateAndReturnLoginResponse() {
        when(normalizeUsernameService.normalizeUsername(DEFAULT_USERNAME))
                .thenReturn(NORMALIZED_USERNAME);
        when(loginAttemptService.checkLock(NORMALIZED_USERNAME))
                .thenReturn(Boolean.FALSE);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtAccessTokenService.generateAccessToken(any(UserDetails.class))).thenReturn(ACCESS_TOKEN);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        LoginRequest loginRequest = validLoginRequest();

        LoginResponse loginResponse = userAccountLoginService.login(loginRequest);

        assertNotNull(loginResponse);
        assertEquals(ACCESS_TOKEN, loginResponse.accessToken());

        verifyLoginOrder(loginRequest);

    }

    @Test
    void login_whenBadCredentials_thenThrowBadCredentialsExceptionAndNotGenerateToken() {
        when(normalizeUsernameService.normalizeUsername(DEFAULT_USERNAME))
                .thenReturn(NORMALIZED_USERNAME);
        when(loginAttemptService.checkLock(NORMALIZED_USERNAME)).thenReturn(Boolean.FALSE);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException(
                ErrorCode.BAD_CREDENTIAL.getDefaultMessage()
        ));

        LoginRequest loginRequest = validLoginRequest();

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> userAccountLoginService.login(loginRequest)
        );

        assertEquals(
                ErrorCode.BAD_CREDENTIAL.getDefaultMessage(),
                exception.getMessage()
        );

        verify(normalizeUsernameService)
                .normalizeUsername(loginRequest.username());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verifyNoInteractions(jwtAccessTokenService);
    }

    private LoginRequest validLoginRequest() {
        return loginRequest(DEFAULT_USERNAME, VALID_PASSWORD);
    }

    private LoginRequest loginRequest(String username, String password) {
        return new LoginRequest(username, password);
    }


    private void verifyLoginOrder(LoginRequest request) {
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        InOrder inOrder = inOrder(
                normalizeUsernameService,
                authenticationManager,
                authentication,
                jwtAccessTokenService
        );

        inOrder.verify(normalizeUsernameService)
                .normalizeUsername(request.username());

        inOrder.verify(authenticationManager)
                .authenticate(tokenCaptor.capture());

        inOrder.verify(authentication)
                .getPrincipal();

        inOrder.verify(jwtAccessTokenService)
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