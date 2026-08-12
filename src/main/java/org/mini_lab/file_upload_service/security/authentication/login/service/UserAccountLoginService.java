package org.mini_lab.file_upload_service.security.authentication.login.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginRequest;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.authentication.login.exception.UserAccountLockedException;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.mini_lab.file_upload_service.security.jwt.service.JwtAccessTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountLoginService {
    private final NormalizeUsernameService normalizeUsernameService;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    @WithSpan("user-account-login-service-login")
    public LoginResponse login(LoginRequest request) {
        String normalizedUsername = getNormalizedUsername(request);

        checkLock(normalizedUsername);
        Authentication authentication = authenticate(request, normalizedUsername);

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtAccessTokenService.generateAccessToken(principal);
        resetFailure(normalizedUsername);

        return new LoginResponse(accessToken);

    }

    private String getNormalizedUsername(LoginRequest request) {
        return normalizeUsernameService.normalizeUsername(request.username());
    }

    private void resetFailure(String normalizedUsername) {
        loginAttemptService.resetFailure(normalizedUsername);
    }

    private @NonNull Authentication authenticate(LoginRequest request, String normalizedUsername) {
        try {
            return authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            normalizedUsername,
                            request.password()
                    )
            );

        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(normalizedUsername);
            throw e;
        }
    }

    private void checkLock(String normalizedUsername) {
        if (loginAttemptService.checkLock(normalizedUsername)) {
            log.error("USER_ACCOUNT_LOCKED username={}", normalizedUsername);
            throw new UserAccountLockedException();
        }
    }
}
