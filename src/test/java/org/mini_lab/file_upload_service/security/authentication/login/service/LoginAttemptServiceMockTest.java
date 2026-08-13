package org.mini_lab.file_upload_service.security.authentication.login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.security.authentication.login.properties.LoginAttemptProperties;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.support.MockUserBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceMockTest {

    @InjectMocks
    LoginAttemptService loginAttemptService;

    @Mock
    LoginAttemptProperties loginAttemptProperties;

    @Mock
    UserRepository userRepository;

    @Mock
    Clock clock;


    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2026-08-21T10:00:00Z"),
                ZoneOffset.UTC
        );

        loginAttemptService = new LoginAttemptService(
                loginAttemptProperties,
                userRepository,
                clock
        );
    }

    @Test
    void checkLock_whenUserLocked_thenReturnTrue() {
        String username = MockUserBuilder.NORMALIZED_USERNAME;
        when(userRepository
                .existsByUsernameAndLockedUntilAfter(username, LocalDateTime.now(clock)))
                .thenReturn(Boolean.TRUE);
        assertThat(loginAttemptService.checkLock(username))
                .isTrue();
    }

    @Test
    void checkLock_whenUserNotLocked_thenReturnFalse() {
        String username = MockUserBuilder.NORMALIZED_USERNAME;
        when(userRepository
                .existsByUsernameAndLockedUntilAfter(username, LocalDateTime.now(clock))
        )
                .thenReturn(Boolean.FALSE);
        assertThat(loginAttemptService.checkLock(username))
                .isFalse();
    }


    @Test
    void recordFailure_whenUsernameIsValid_thenVerifyInteractUserRepository() {
        when(loginAttemptProperties.getFailureAttempts())
                .thenReturn(5);

        when(loginAttemptProperties.getLockDuration())
                .thenReturn(Duration.ofMinutes(60));

        LocalDateTime now = LocalDateTime.now(clock);

        String username = MockUserBuilder.NORMALIZED_USERNAME;

        loginAttemptService.recordFailure(username);

        verify(userRepository)
                .recordLoginFailureCount(
                        eq(username),
                        eq(now.plusMinutes(60)),
                        eq(5),
                        eq(now)
                );
    }


    @Test
    void resetFailure_whenUsernameIsValid_thenVerifyInteractUserRepositiry() {

        LocalDateTime now = LocalDateTime.now(clock);

        String username = MockUserBuilder.NORMALIZED_USERNAME;

        loginAttemptService.resetFailure(username);

        verify(userRepository)
                .resetFailureCount(
                        eq(username),
                        eq(now)
                );

    }

    @Test
    void resetFailure_whenUsernameIsNull_thenShouldThrowException() {
        assertThatThrownBy(() -> loginAttemptService.recordFailure(null));
        verifyNoInteractions(userRepository);
    }


    @Test
    void checkLock_whenUsernameIsNull_thenThrowException() {
        assertThatThrownBy(() -> loginAttemptService.checkLock(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    void recordFailure_whenUsernameIsNull_thenThrowException() {
        assertThatThrownBy(() -> loginAttemptService.recordFailure(null));
        verifyNoInteractions(userRepository);
    }

}