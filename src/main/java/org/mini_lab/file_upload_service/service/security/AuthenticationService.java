package org.mini_lab.file_upload_service.service.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.security.RegisterRequest;
import org.mini_lab.file_upload_service.dto.security.RegisterResponse;
import org.mini_lab.file_upload_service.entity.User;
import org.mini_lab.file_upload_service.exception.security.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
}
