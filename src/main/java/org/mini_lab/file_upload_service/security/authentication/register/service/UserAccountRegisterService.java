package org.mini_lab.file_upload_service.security.authentication.register.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterResponse;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.register.exception.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.mini_lab.file_upload_service.security.authentication.shared.service.PasswordVerifyService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountRegisterService {

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
