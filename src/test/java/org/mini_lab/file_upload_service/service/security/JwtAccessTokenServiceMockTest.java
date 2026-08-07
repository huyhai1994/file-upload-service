package org.mini_lab.file_upload_service.service.security;

import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.security.jwt.components.JwtAccessTokenValidator;
import org.mini_lab.file_upload_service.security.jwt.configuration.JwtProperties;
import org.mini_lab.file_upload_service.security.jwt.dto.AccessTokenPayload;
import org.mini_lab.file_upload_service.security.jwt.service.JwtAccessTokenService;
import org.mini_lab.file_upload_service.support.MockAccessTokenBuilder;
import org.mini_lab.file_upload_service.support.MockUserBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mini_lab.file_upload_service.support.MockTimeBuilder.NOW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenServiceMockTest {

    private static final Duration ACCESS_TOKEN_EXPIRATION =
            Duration.ofMinutes(15);

    private static final String SECRET_KEY =
            "GpP5Ko/lSAodfqg3B/TJpcijh06eST3P2M0pn6ZTIYY=";

    private static final String ISSUER = "test-service";

    @Mock
    JwtProperties jwtProperties;

    @Mock
    Clock clock;

    @Mock
    UserDetails userDetails;

    @InjectMocks
    JwtAccessTokenService jwtAccessTokenService;

    @Mock
    JwtAccessTokenValidator accessTokenValidator;

    @Mock
    Claims claims;

    @Test
    void generateAccessToken_whenUserDetailsValid_thenReturnAccessTokenWithExpectedClaims() {
        when(clock.instant()).thenReturn(NOW);
        when(jwtProperties.accessTokenExpiration())
                .thenReturn(ACCESS_TOKEN_EXPIRATION);
        when(jwtProperties.secretKey()).thenReturn(SECRET_KEY);
        when(jwtProperties.issuer()).thenReturn(ISSUER);

        when(userDetails.getUsername())
                .thenReturn(MockUserBuilder.NORMALIZED_USERNAME);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        String accessToken = jwtAccessTokenService.generateAccessToken(userDetails);

        assertThat(accessToken)
                .isNotNull()
                .isNotBlank();

        Claims claims = jwtAccessTokenService.extractClaims(accessToken);

        assertThat(claims.getSubject())
                .isEqualTo(MockUserBuilder.NORMALIZED_USERNAME);

        assertThat(claims.getIssuer())
                .isEqualTo(ISSUER);

        assertThat(claims.getIssuedAt().toInstant())
                .isEqualTo(NOW);

        assertThat(claims.getExpiration().toInstant())
                .isEqualTo(NOW.plus(ACCESS_TOKEN_EXPIRATION));
    }

    @Test
    void generateAccessToken_whenUserDetailsIsNull_thenThrowException() {
        doThrow(new NullPointerException()).when(accessTokenValidator).validateUserDetails(any(UserDetails.class));
        assertThrows(NullPointerException.class, () -> jwtAccessTokenService.generateAccessToken(userDetails));
        verify(accessTokenValidator).validateUserDetails(any(UserDetails.class));
        verifyNoInteractions(clock, jwtProperties);
    }


    @Test
    void extractClaims_whenTokenValid_thenReturnExpectedClaims() {
        // Given
        when(clock.instant()).thenReturn(NOW);

        when(jwtProperties.accessTokenExpiration())
                .thenReturn(ACCESS_TOKEN_EXPIRATION);

        when(jwtProperties.secretKey())
                .thenReturn(SECRET_KEY);

        when(jwtProperties.issuer())
                .thenReturn(ISSUER);

        when(userDetails.getUsername())
                .thenReturn(MockUserBuilder.NORMALIZED_USERNAME);

        when(userDetails.getAuthorities())
                .thenReturn(List.of());

        String token =
                jwtAccessTokenService.generateAccessToken(userDetails);

        // When
        Claims claims =
                jwtAccessTokenService.extractClaims(token);

        // Then
        assertThat(claims.getSubject())
                .isEqualTo(MockUserBuilder.NORMALIZED_USERNAME);

        assertThat(claims.getIssuer())
                .isEqualTo(ISSUER);

        assertThat(claims.getIssuedAt().toInstant())
                .isEqualTo(NOW);

        assertThat(claims.getExpiration().toInstant())
                .isEqualTo(
                        NOW.plus(ACCESS_TOKEN_EXPIRATION)
                );
    }

    @Test
    void parseAndValidate_whenAccessTokenValid_thenReturnPayload() {
        // Given
        when(clock.instant()).thenReturn(NOW);

        when(jwtProperties.accessTokenExpiration())
                .thenReturn(ACCESS_TOKEN_EXPIRATION);

        when(jwtProperties.secretKey())
                .thenReturn(SECRET_KEY);

        when(jwtProperties.issuer())
                .thenReturn(ISSUER);

        when(userDetails.getUsername())
                .thenReturn(MockUserBuilder.NORMALIZED_USERNAME);

        when(userDetails.getAuthorities())
                .thenReturn(List.of());

        String token =
                jwtAccessTokenService.generateAccessToken(userDetails);

        when(accessTokenValidator.validateRawAuthorities(
                List.of()
        )).thenReturn(List.of());

        AccessTokenPayload payload =
                jwtAccessTokenService.parseAndValidate(token);
        assertThat(payload.username())
                .isEqualTo(MockUserBuilder.NORMALIZED_USERNAME);

    }

    @Test
    void generateAccessToken_whenUserDetailsNull_thenThrowNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> jwtAccessTokenService.generateAccessToken(null)
        );
    }
}