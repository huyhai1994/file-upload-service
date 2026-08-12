package org.mini_lab.file_upload_service.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.security.configuration.PasswordEncoderTest;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockUserBuilder;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PasswordEncoderTest.class)
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    Clock clock;

    @Autowired
    TransactionTemplate transactionTemplate;

    private static final Instant NOW =
            Instant.parse("2026-08-11T02:00:00Z");

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void existsByUsername_whenUsernameExists_thenReturnTrue() {

        String username = "test-user";

        persistUser(username);

        entityManager.clear();

        assertTrue(
                userRepository.existsByUsername(username)
        );
    }

    @Test
    void existsByUsername_whenUsernameDoesNotExist_thenReturnFalse() {

        assertFalse(
                userRepository.existsByUsername("unknown")
        );
    }

    @Test
    void saveAndFlush_whenPersistExistedUsername_thenThrowException() {
        persistUser(MockUserBuilder.NORMALIZED_USERNAME);
        assertThrows(DataIntegrityViolationException.class,
                () -> persistUser(MockUserBuilder.NORMALIZED_USERNAME));
    }

    @Test
    void existsByUsernameAndLockedUntilIsNotNull_whenUserAccountNotLocked_thenLockUntilIsNull() {
        persistUser(MockUserBuilder.NORMALIZED_USERNAME);
        assertThat(userRepository.existsByUsernameAndLockedUntilIsNotNull(MockUserBuilder.NORMALIZED_USERNAME)).isFalse();
    }

    @Test
    void existsByUsernameAndLockedUntilIsNotNull_whenUserAccountLocked_thenLockUntilIsNotNull() {

        persistLockedUser(MockUserBuilder.NORMALIZED_USERNAME);

        assertThat(
                userRepository
                        .existsByUsernameAndLockedUntilIsNotNull(
                                MockUserBuilder.NORMALIZED_USERNAME
                        )
        )
                .isTrue();

        entityManager.clear();

        User persistedUser = userRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(persistedUser.getLockedUntil())
                .isEqualTo(LocalDateTime.now(clock).plusHours(1));


    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void recordLoginFailedCount_when6ConcurrentLoginRequestFailed_thenAccountIsLockedAndUpdateLockedUntil() throws ExecutionException, InterruptedException, TimeoutException {

        persistUser(MockUserBuilder.NORMALIZED_USERNAME);

        User persistedUser = userRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        UUID uuid = persistedUser.getId();

        try (RaceConditionSimulator raceConditionSimulator =
                     RaceConditionSimulator.getRaceConditionSimulator(6)) {
            List<Integer> affectRows = raceConditionSimulator.execute(
                    () -> transactionTemplate.execute(
                            status ->
                                    userRepository
                                            .recordLoginFailureCount(
                                                    uuid,
                                                    LocalDateTime.now(clock).plusHours(1),
                                                    5,
                                                    LocalDateTime.now(clock))

                    )
            );

            assertThat(affectRows).containsExactlyInAnyOrder(1, 1, 1, 1, 1, 1);
        }

        entityManager.clear();
        persistedUser = userRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(persistedUser.getLockedUntil())
                .isEqualTo(LocalDateTime.now(clock).plusHours(1));
        assertThat(persistedUser.getUpdatedAt())
                .isEqualTo(LocalDateTime.now(clock));
        assertThat(persistedUser.getFailedLoginCount())
                .isEqualTo(6);

        userRepository.deleteAllInBatch();
    }

    @Test
    void resetFailureCount_whenFailureCountBiggerThan0_thenResetTo0AndFailedLoginCountIsNull() {
        persistLockedUser(MockUserBuilder.NORMALIZED_USERNAME);
        User persistLockedUser = userRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();
        UUID uuid = persistLockedUser.getId();
        assertThat(userRepository.resetFailureCount(uuid)).isOne();

        entityManager.flush();
        entityManager.clear();

        int totalUser = Math.toIntExact(userRepository.findAll().size());
        assertThat(totalUser).isOne();

        User resetLockedUser = userRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(resetLockedUser.getLockedUntil()).isNull();
        assertThat(resetLockedUser.getFailedLoginCount()).isZero();
    }


    private void persistUser(String username) {

        userRepository.saveAndFlush(
                new User(
                        username,
                        passwordEncoder.encode("passwordHash")
                )
        );
    }

    private void persistLockedUser(String username) {
        User lockedUser =
                new User(
                        username,
                        passwordEncoder.encode("passwordHash")
                );
        lockedUser.setLockedUntil(LocalDateTime.now(clock).plusHours(1));
        lockedUser.setFailedLoginCount(5);

        userRepository.saveAndFlush(lockedUser);
    }
}