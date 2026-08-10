package org.mini_lab.file_upload_service.security.jwt.filter;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    public static final String USERS_ME = "/api/v1/users/me";

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JacksonUtils jacksonUtils;


    @BeforeEach
    void cleanUp() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser("test")
    void protectedEndpoint_whenTokenIsMissing_thenReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get(USERS_ME)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void performLogin_thenReturnAccessToken() throws Exception {
        persistAnValidUser();
        MvcResult loginResult =
                performLogin(NORMALIZED_USERNAME, MockPasswordBuilder.RAW_PASSWORD)
                        .andExpect(status().isOk())
                        .andReturn();

        ApiResponse<LoginResponse> response =
                jacksonUtils.convertFromJson(
                        loginResult.getResponse().getContentAsString(),
                        new TypeReference<>() {
                        }
                );
        String accessToken = response.data().accessToken();
        assertThat(accessToken).isNotNull();

    }

    @Test
    void protectedEndpoint_whenAccessTokenIsValid_thenAuthenticateUser()
            throws Exception {

        // Given
        persistAnValidUser();

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
        mockMvc.perform(
                        get(USERS_ME)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk());
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


}