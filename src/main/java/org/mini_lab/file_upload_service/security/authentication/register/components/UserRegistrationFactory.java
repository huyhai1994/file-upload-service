package org.mini_lab.file_upload_service.security.authentication.register.components;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.register.service.UsernameVerifyService;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.shared.service.NormalizeUsernameService;
import org.mini_lab.file_upload_service.security.authentication.shared.service.PasswordVerifyService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegistrationFactory {

    private final NormalizeUsernameService normalizeUsernameService;
    private final UsernameVerifyService usernameVerifyService;
    private final PasswordVerifyService passwordVerifyService;
    private final PasswordEncoder passwordEncoder;

    public User createUser(RegisterRequest request) {
        String normalizedUsername = normalizeUsernameService.normalizeUsername(request.username());

        usernameVerifyService.verify(normalizedUsername);
        passwordVerifyService.verify(request.password());

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                normalizedUsername,
                passwordHash,
                request.emailAddress()
        );
        return user;

    }

}
