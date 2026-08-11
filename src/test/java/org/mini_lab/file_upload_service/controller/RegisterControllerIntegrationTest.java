package org.mini_lab.file_upload_service.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.file_upload.enums.ErrorCode;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.DEFAULT_USERNAME;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.mini_lab.file_upload_service.support.RaceConditionSimulator.getRaceConditionSimulator;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RegisterControllerIntegrationTest
        extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";

    private static final String LOGIN_URL = "/api/v1/auth/login";

    private static final String VALID_PASSWORD = "password123";

    private static final int CONCURRENT_REQUEST_COUNT = 2;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();

        executorService = Executors.newFixedThreadPool(
                CONCURRENT_REQUEST_COUNT
        );
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();

        boolean terminated = executorService.awaitTermination(
                5,
                TimeUnit.SECONDS
        );

        assertThat(terminated).isTrue();
    }

    @Test
    void register_whenRequestBodyValidationFails_thenReturnBadRequest()
            throws Exception {

        ResultActions result = performRegister("", "");

        assertValidationError(
                result,
                ErrorCode.VALIDATION_ERROR
        );

        assertNoUserSaved();
    }

    @Test
    void register_whenPasswordIsTooShort_thenReturnBadRequest()
            throws Exception {

        ResultActions result = performRegister(
                NORMALIZED_USERNAME,
                "1234567"
        );

        assertValidationError(
                result,
                ErrorCode.PASSWORD_TOO_SHORT
        );

        assertNoUserSaved();
    }

    @Test
    void register_whenPasswordIsTooLong_thenReturnBadRequest()
            throws Exception {

        ResultActions result = performRegister(
                NORMALIZED_USERNAME,
                "a".repeat(73)
        );

        assertValidationError(
                result,
                ErrorCode.PASSWORD_LENGTH_EXCEEDED
        );

        assertNoUserSaved();
    }

    @Test
    void register_whenUsernameIsTooLong_thenReturnBadRequest()
            throws Exception {

        ResultActions result = performRegister(
                "a".repeat(100),
                VALID_PASSWORD
        );

        assertValidationError(
                result,
                ErrorCode.USERNAME_LENGTH_EXCEEDED
        );

        assertNoUserSaved();
    }

    @Test
    void login_whenLoginSucceeds_thenReturnAccessToken() throws Exception {
        persistAnValidUser();
        performLogin(DEFAULT_USERNAME, VALID_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void login_whenUsernameNotFound_thenThrowBadCredentails() throws Exception {
        performLogin(DEFAULT_USERNAME, VALID_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_CREDENTIALS.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INVALID_CREDENTIALS.getDefaultMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void login_whenPasswordNotValid_thenThrowBadCredentails() throws Exception {
        persistAnValidUser();
        performLogin(DEFAULT_USERNAME, "not-valid-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_CREDENTIALS.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INVALID_CREDENTIALS.getDefaultMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    private void persistAnValidUser() {
        User user = new User(NORMALIZED_USERNAME, passwordEncoder.encode(VALID_PASSWORD));
        userRepository.saveAndFlush(user);
    }

    @Test
    void register_whenRegisterSucceeds_thenReturnCreatedAndPersistUser()
            throws Exception {

        performRegister(DEFAULT_USERNAME, VALID_PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username")
                        .value(NORMALIZED_USERNAME));

        User savedUser = getOnlySavedUser();

        assertThat(savedUser.getUsername())
                .isEqualTo(NORMALIZED_USERNAME);

        assertThat(savedUser.getPasswordHash())
                .isNotEqualTo(VALID_PASSWORD);

        assertThat(
                passwordEncoder.matches(
                        VALID_PASSWORD,
                        savedUser.getPasswordHash()
                )
        ).isTrue();
    }

    @Test
    void register_whenNormalizedUsernameAlreadyExists_thenReturnConflict()
            throws Exception {

        performRegister(DEFAULT_USERNAME, VALID_PASSWORD)
                .andExpect(status().isCreated());

        performRegister(
                DEFAULT_USERNAME,
                "anotherPassword123"
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(ErrorCode.USERNAME_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.USERNAME_ALREADY_EXISTS
                                .getDefaultMessage()));

        assertThat(userRepository.count()).isEqualTo(1);

        User savedUser = getOnlySavedUser();

        assertThat(savedUser.getUsername())
                .isEqualTo(NORMALIZED_USERNAME);

        assertThat(
                passwordEncoder.matches(
                        VALID_PASSWORD,
                        savedUser.getPasswordHash()
                )
        ).isTrue();
    }

    @Test
    void register_whenTwoRequestsUseSameUsernameConcurrently_thenOneCreatedAndOneConflict()
            throws Exception {

        String requestBody = createRequestBody(
                " ConcurrentUser ",
                VALID_PASSWORD
        );

        try (RaceConditionSimulator raceConditionSimulator = getRaceConditionSimulator(CONCURRENT_REQUEST_COUNT)) {
            List<MvcResult> mvcResults = raceConditionSimulator.execute(() -> performRegister(requestBody));

            List<Integer> responseStatuses = mvcResults.stream()
                    .map(MvcResult::getResponse)
                    .map(MockHttpServletResponse::getStatus)
                    .toList();

            assertThat(responseStatuses)
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
                            VALID_PASSWORD,
                            savedUser.getPasswordHash()
                    )
            ).isTrue();
        }

    }

    private ResultActions performRegister(
            String username,
            String password
    ) throws Exception {

        return mockMvc.perform(
                post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(username, password))
        );
    }

    private ResultActions performLogin(
            String username,
            String password
    ) throws Exception {

        return mockMvc.perform(
                post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestBody(username, password))
        );
    }

    private MvcResult performLogin(String requestBody) throws Exception {
        return mockMvc.perform(
                        post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andReturn();
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

    private String createRequestBody(
            String username,
            String password
    ) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);
    }

    private void assertValidationError(
            ResultActions result,
            ErrorCode errorCode
    ) throws Exception {

        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(errorCode.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(errorCode.getDefaultMessage()));
    }

    private void assertNoUserSaved() {
        assertThat(userRepository.count()).isZero();
    }

    private User getOnlySavedUser() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(1);

        return users.get(0);
    }
}