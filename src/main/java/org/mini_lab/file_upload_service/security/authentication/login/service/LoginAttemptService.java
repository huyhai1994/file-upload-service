package org.mini_lab.file_upload_service.security.authentication.login.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.login.properties.LoginAttemptProperties;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final LoginAttemptProperties loginAttemptProperties;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public void recordFailure(String username) {
        Objects.requireNonNull(username);
        LocalDateTime now = LocalDateTime.now(clock);
        userRepository.recordLoginFailureCount(
                username,
                now.plusMinutes(loginAttemptProperties.getLockDuration().toMinutes()),
                loginAttemptProperties.getFailureAttempts(),
                now);
    }

    @Transactional
    public void resetFailure(String username) {
        Objects.requireNonNull(username);
        LocalDateTime now = LocalDateTime.now(clock);
        userRepository.resetFailureCount(username, now);
    }

    @Transactional
    public boolean checkLock(String username) {
        Objects.requireNonNull(username);
        return userRepository.existsByUsernameAndLockedUntilIsNotNull(username);
    }
}
