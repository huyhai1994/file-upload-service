package org.mini_lab.file_upload_service.security.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.dto.LoginRequest;
import org.mini_lab.file_upload_service.security.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.dto.RegisterResponse;
import org.mini_lab.file_upload_service.security.entity.User;
import org.mini_lab.file_upload_service.security.exception.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.security.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NormalizeUsernameService normalizeUsernameService;
    private final UsernameVerifyService usernameVerifyService;
    private final PasswordVerifyService passwordVerifyService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedUsername = normalizeUsernameService.normalizeUsername(request.username());

        usernameVerifyService.verify(normalizedUsername);
        passwordVerifyService.verify(request.password());

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                normalizedUsername,
                passwordHash
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getUsername()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }
    }

    @WithSpan("authenticationservice-login")
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
        String accessToken = jwtService.generateAccessToken(principal);

        return new LoginResponse(accessToken);

    }
}
