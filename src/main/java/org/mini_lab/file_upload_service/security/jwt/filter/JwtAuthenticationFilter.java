package org.mini_lab.file_upload_service.security.jwt.filter;

import io.jsonwebtoken.JwtException;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.security.jwt.dto.AccessTokenPayload;
import org.mini_lab.file_upload_service.security.jwt.dto.AuthenticatedUser;
import org.mini_lab.file_upload_service.security.jwt.service.JwtAccessTokenService;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final JwtAccessTokenService jwtAccessTokenService;


    @Override
    @WithSpan("jwt-authentication-filter-do-filter-internal")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info(
                "JWT_FILTER method={} uri={} dispatcher={} trace={} auth={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getDispatcherType(),
                MDC.get("trace_id"),
                request.getHeader(HttpHeaders.AUTHORIZATION) != null
        );
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }


        try {
            String accessToken = authorization
                    .substring(BEARER_PREFIX.length())
                    .trim();
            AccessTokenPayload payload =
                    jwtAccessTokenService.parseAndValidate(accessToken);

            AuthenticatedUser principal = new AuthenticatedUser(
                    payload.username(),
                    payload.authorities()
            );

            Authentication authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            principal,
                            null,
                            List.of()
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

        } catch (NullPointerException | JwtException exception) {
            SecurityContextHolder.clearContext();

            AuthenticationException authenticationException =
                    new BadCredentialsException(
                            "Access token is invalid",
                            exception
                    );

            authenticationEntryPoint.commence(
                    request,
                    response,
                    authenticationException
            );
            return;
        }
        filterChain.doFilter(request, response);
    }
}
