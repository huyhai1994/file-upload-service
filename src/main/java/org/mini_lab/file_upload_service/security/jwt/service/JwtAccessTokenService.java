package org.mini_lab.file_upload_service.security.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.jwt.configuration.JwtProperties;
import org.mini_lab.file_upload_service.security.jwt.dto.AccessTokenPayload;
import org.mini_lab.file_upload_service.security.jwt.error_code.ErrorCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtAccessTokenService {

    private static final String AUTHORITIES_CLAIM = "authorities";

    private final JwtProperties jwtProperties;
    private final Clock clock;

    @WithSpan("jwtservice-generate-access-token")
    public String generateAccessToken(UserDetails userDetails) {
        Objects.requireNonNull(
                userDetails,
                ErrorCode.USER_DETAILS_REQUIRED.getDefaultMessage()
        );

        Instant issuedAt = clock.instant();
        Instant expiresAt =
                issuedAt.plus(jwtProperties.accessTokenExpiration());

        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.issuer())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(AUTHORITIES_CLAIM, authorities)
                .signWith(getSigningKey())
                .compact();
    }

    @WithSpan("jwtservice-parse-access-token")
    public AccessTokenPayload parseAndValidate(String accessToken) {
        Objects.requireNonNull(
                accessToken,
                ErrorCode.ACCESS_TOKEN_REQUIRED.getDefaultMessage()
        );

        Claims claims = extractClaims(accessToken);

        String username = claims.getSubject();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.JWT_SUBJECT_REQUIRED.getDefaultMessage());
        }

        List<String> authorities =
                extractAuthoritiesFromClaims(claims);

        return new AccessTokenPayload(
                username,
                authorities
        );
    }

    public Claims extractClaims(String token) {
        Objects.requireNonNull(
                token,
                ErrorCode.ACCESS_TOKEN_REQUIRED.getDefaultMessage()
        );

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(jwtProperties.issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<String> extractAuthoritiesFromClaims(
            Claims claims
    ) {
        Object authoritiesClaim =
                claims.get(AUTHORITIES_CLAIM);

        if (!(authoritiesClaim instanceof List<?> rawAuthorities)) {
            throw new IllegalArgumentException(
                    ErrorCode.AUTHORIZE_CLAIMS_IS_ARRAY.getDefaultMessage()
            );
        }

        return rawAuthorities.stream()
                .map(authority -> {
                    if (!(authority instanceof String value)) {
                        throw new IllegalArgumentException(
                                ErrorCode.AUTHORITY_IS_STRING.getDefaultMessage()
                        );
                    }

                    return value;
                })
                .toList();
    }

    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder()
                .decode(jwtProperties.secretKey());

        return Keys.hmacShaKeyFor(decodedKey);
    }
}