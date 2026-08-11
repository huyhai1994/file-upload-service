package org.mini_lab.file_upload_service.security.authentication.login.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginRequest;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.mini_lab.file_upload_service.security.jwt.service.JwtAccessTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountLoginService {
    private final NormalizeUsernameService normalizeUsernameService;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final AuthenticationManager authenticationManager;

    @WithSpan("user-account-login-service-login")
    public LoginResponse login(LoginRequest request) {
        String normalizedUsername = normalizeUsernameService.normalizeUsername(request.username());

        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                normalizedUsername,
                                request.password()
                        )
                );

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtAccessTokenService.generateAccessToken(principal);

        return new LoginResponse(accessToken);

    }
}
