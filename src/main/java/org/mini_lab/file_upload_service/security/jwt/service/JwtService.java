package org.mini_lab.file_upload_service.security.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.jwt.configuration.JwtProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String AUTHORITIES_CLAIM = "authorities";

    private final JwtProperties jwtProperties;
    private final Clock clock;

    @WithSpan("jwtservice-generate-access-token")
    public String generateAccessToken(UserDetails userDetails) {
        Objects.requireNonNull(userDetails);

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

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> extractAuthorities(String token) {
        return extractClaims(token)
                .get(AUTHORITIES_CLAIM, List.class);
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        Claims claims = extractClaims(token);

        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getIssuer().equals(jwtProperties.issuer())
                && claims.getExpiration().after(Date.from(clock.instant()));
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .requireIssuer(jwtProperties.issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSigningKey() {
        byte[] decodedKey = Base64.getDecoder()
                .decode(jwtProperties.secretKey());

        return Keys.hmacShaKeyFor(decodedKey);
    }
}
