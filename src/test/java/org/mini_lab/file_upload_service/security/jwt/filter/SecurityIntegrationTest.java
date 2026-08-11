package org.mini_lab.file_upload_service.security.jwt.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import io.prometheus.metrics.shaded.com_google_protobuf_4_34_0.DurationOrBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.authentication.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.authentication.entity.User;
import org.mini_lab.file_upload_service.security.authentication.repository.UserRepository;
import org.mini_lab.file_upload_service.shared.json.JacksonUtils;
import org.mini_lab.file_upload_service.shared.response.ApiResponse;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockPasswordBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    public static final String PROTECTED_ENDPOINT = "/api/v1/users/me";

    private static final Instant NOW =
            Instant.parse("2026-08-11T02:00:00Z");

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JacksonUtils jacksonUtils;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAllInBatch();
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    @WithMockUser("test")
    void protectedEndpoint_whenTokenIsMissing_thenReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get(PROTECTED_ENDPOINT)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void performLogin_thenReturnAccessToken() throws Exception {
        persistAnValidUser();
        String accessToken = performLoginAndGetAccessToken();
        assertThat(accessToken).isNotNull();

    }

    @Test
    void protectedEndpoint_whenAccessTokenIsValid_thenAuthenticateUser()
            throws Exception {
        persistAnValidUser();
        String accessToken = performLoginAndGetAccessToken();
        performProtectedRequest(accessToken).andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_whenAccessTokenIsNull_thenReturnUnAuthorized() throws Exception {
        mockMvc.perform(
                        get(PROTECTED_ENDPOINT)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + null
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_whenInvalidSignature_thenReturnUnAuthorized() throws Exception {
        persistAnValidUser();

        String accessToken = performLoginAndGetAccessToken();
        String tokenWithInvalidSignature = tamperSignature(accessToken);
        performProtectedRequest(tokenWithInvalidSignature).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_whenTokenExpired_thenReturnUnAuthorized() throws Exception {
        persistAnValidUser();
        when(clock.instant()).thenReturn(NOW.minus(Duration.ofHours(1)));

        String expiredToken = performLoginAndGetAccessToken();
        when(clock.instant()).thenReturn(NOW);
        performProtectedRequest(expiredToken)
                .andExpect(status().isUnauthorized());
    }

    private String tamperSignature(String token) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT");
        }

        String signature = parts[2];

        char firstCharacter = signature.charAt(0);

        char replacement =
                firstCharacter == 'A'
                        ? 'B'
                        : 'A';

        String tamperedSignature =
                replacement + signature.substring(1);

        return parts[0]
                + "."
                + parts[1]
                + "."
                + tamperedSignature;
    }

    private void persistAnValidUser() {
        User user = new User(NORMALIZED_USERNAME, passwordEncoder.encode(MockPasswordBuilder.RAW_PASSWORD));
        userRepository.saveAndFlush(user);
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

    private String performLoginAndGetAccessToken() throws Exception {
        MvcResult loginResult =
                performLogin(
                        NORMALIZED_USERNAME,
                        MockPasswordBuilder.RAW_PASSWORD
                )
                        .andExpect(status().isOk())
                        .andReturn();

        ApiResponse<LoginResponse> response =
                jacksonUtils.convertFromJson(
                        loginResult.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );
        String accessToken = response.data().accessToken();
        return accessToken;
    }

    private ResultActions performProtectedRequest(String accessToken) throws Exception {
        return mockMvc.perform(
                get(PROTECTED_ENDPOINT)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        )
        );
    }

}