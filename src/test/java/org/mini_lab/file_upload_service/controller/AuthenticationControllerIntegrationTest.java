package org.mini_lab.file_upload_service.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.User;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
import org.mini_lab.file_upload_service.repository.UserRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthenticationControllerIntegrationTest
        extends AbstractIntegrationTest {

    private static final String REGISTER_URL =
            "/api/v1/auth/register";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    private ExecutorService executorService;

    private final int REQUEST_COUNTS = 2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();
        executorService = Executors.newFixedThreadPool(REQUEST_COUNTS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void register_whenRequestBodyValidationFails_thenReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.VALIDATION_ERROR.getDefaultMessage()));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void register_whenPasswordValidationFails_thenReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                  "username": "hai",
                  "password": "1234567"
                }
                """;

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.PASSWORD_TOO_SHORT.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.PASSWORD_TOO_SHORT.getDefaultMessage()));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void register_whenUsernameValidationFails_thenReturnBadRequest()
            throws Exception {

        String username = "a".repeat(100);

        String requestBody = """
                {
                  "username": "%s",
                  "password": "password123"
                }
                """.formatted(username);

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.USERNAME_LENGTH_EXCEEDED.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.USERNAME_LENGTH_EXCEEDED
                                .getDefaultMessage()));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void register_whenTwoRegisterRequestsAtSameTime_thenOneSucceedsAndOneFails()
            throws Exception {

        String requestBody = """
                {
                  "username": " ConcurrentUser ",
                  "password": "password123"
                }
                """;

        CountDownLatch readyLatch = new CountDownLatch(REQUEST_COUNTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(REQUEST_COUNTS);

        List<Future<MvcResult>> futures = new ArrayList<>();

        for (int i = 0; i < REQUEST_COUNTS; i++) {
            Future<MvcResult> future = executorService.submit(() -> {
                readyLatch.countDown();

                try {
                    startLatch.await();
                    return performRegister(requestBody);
                } finally {
                    doneLatch.countDown();
                }
            });

            futures.add(future);
        }

        assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();

        startLatch.countDown();

        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();

        List<Integer> statuses = new ArrayList<>();

        for (Future<MvcResult> future : futures) {
            MvcResult result = future.get(10, TimeUnit.SECONDS);
            statuses.add(result.getResponse().getStatus());
        }

        assertThat(statuses)
                .containsExactlyInAnyOrder(
                        HttpStatus.CREATED.value(),
                        HttpStatus.CONFLICT.value()
                );

        assertThat(userRepository.count()).isEqualTo(1);

        User savedUser = userRepository
                .findByUsername("concurrentuser")
                .orElseThrow();

        assertThat(savedUser.getUsername())
                .isEqualTo("concurrentuser");

        assertThat(
                passwordEncoder.matches(
                        "password123",
                        savedUser.getPasswordHash()
                )
        ).isTrue();
    }

    @Test
    void register_whenRegisterSuccess_thenReturnCreated()
            throws Exception {

        String rawPassword = "password123";

        String requestBody = """
                {
                  "username": " Hai ",
                  "password": "%s"
                }
                """.formatted(rawPassword);

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username")
                        .value("hai"));

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(1);

        User savedUser = users.get(0);

        assertThat(savedUser.getUsername())
                .isEqualTo("hai");

        assertThat(savedUser.getPasswordHash())
                .isNotEqualTo(rawPassword);

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPasswordHash()
                )
        ).isTrue();
    }

    @Test
    void register_whenPasswordIsTooLong_thenReturnBadRequest()
            throws Exception {

        String password = "a".repeat(73);

        String requestBody = """
                {
                  "username": "hai",
                  "password": "%s"
                }
                """.formatted(password);

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.PASSWORD_LENGTH_EXCEEDED.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.PASSWORD_LENGTH_EXCEEDED
                                .getDefaultMessage()));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void register_whenUsernameAlreadyExists_thenReturnConflict()
            throws Exception {

        String firstRequest = """
                {
                  "username": "Hai",
                  "password": "password123"
                }
                """;

        String secondRequest = """
                {
                  "username": " hai ",
                  "password": "anotherPassword123"
                }
                """;

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.USERNAME_ALREADY_EXISTS.name()));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    private MvcResult performRegister(String requestBody)
            throws Exception {

        return mockMvc.perform(
                        post(REGISTER_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andReturn();
    }
}