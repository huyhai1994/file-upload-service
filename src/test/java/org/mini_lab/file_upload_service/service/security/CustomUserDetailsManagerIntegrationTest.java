package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.entity.User;
import org.mini_lab.file_upload_service.security.repository.UserRepository;
import org.mini_lab.file_upload_service.security.service.CustomUserDetailsManager;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.RAW_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;

@SpringBootTest
@ActiveProfiles("test")
class CustomUserDetailsManagerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CustomUserDetailsManager customUserDetailsManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAllInBatch();
    }

    @Test
    void loadUserByUsername_whenUserExists_thenReturnUserDetails() {
        String encodedPassword = passwordEncoder.encode(RAW_PASSWORD);

        User user = new User(
                NORMALIZED_USERNAME,
                encodedPassword
        );

        User persistedUser = userRepository.saveAndFlush(user);

        UserDetails userDetails =
                customUserDetailsManager.loadUserByUsername(
                        persistedUser.getUsername()
                );

        assertTrue(
                passwordEncoder.matches(
                        RAW_PASSWORD,
                        userDetails.getPassword()
                )
        );
    }

    @Test
    void loadUserByUsername_whenUserNotExists_thenThrowsUsernameNotFoundException() {
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsManager.loadUserByUsername(
                        NORMALIZED_USERNAME
                ));
    }


}