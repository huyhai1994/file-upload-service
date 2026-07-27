package org.mini_lab.file_upload_service.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.User;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

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

    private void persistUser(String username) {

        userRepository.saveAndFlush(
                new User(
                        username,
                        "passwordHash"
                )
        );
    }

}