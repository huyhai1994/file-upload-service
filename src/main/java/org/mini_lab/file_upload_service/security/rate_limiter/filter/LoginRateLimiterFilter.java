package org.mini_lab.file_upload_service.security.rate_limiter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mini_lab.file_upload_service.file_upload.enums.ErrorCode;
import org.mini_lab.file_upload_service.security.rate_limiter.component.IPAddressDetector;
import org.mini_lab.file_upload_service.security.rate_limiter.component.IdentityHasher;
import org.mini_lab.file_upload_service.security.rate_limiter.exceptions.LoginRateLimitExceededException;
import org.mini_lab.file_upload_service.security.rate_limiter.exceptions.RateLimiterUnavailableException;
import org.mini_lab.file_upload_service.security.rate_limiter.service.LoginRateLimitService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class LoginRateLimiterFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String IP_IDENTITY_PREFIX = "IP|";

    private final LoginRateLimitService loginRateLimitService;
    private final IPAddressDetector ipAddressDetector;
    private final IdentityHasher identityHasher;
    private final HandlerExceptionResolver exceptionResolver;

    public LoginRateLimiterFilter(
            @Qualifier("redisLoginRateLimitService")
            LoginRateLimitService loginRateLimitService,
            IPAddressDetector ipAddressDetector,
            IdentityHasher identityHasher,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver
    ) {
        this.loginRateLimitService = loginRateLimitService;
        this.ipAddressDetector = ipAddressDetector;
        this.identityHasher = identityHasher;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !LOGIN_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String identityHash = createIdentityHash(request);

        try {
            enforceRateLimit(identityHash);
        } catch (LoginRateLimitExceededException |
                 RateLimiterUnavailableException exception) {

            resolveException(request, response, exception);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String createIdentityHash(HttpServletRequest request) {
        String ipAddress = ipAddressDetector.detect(request);
        return identityHasher.hash(IP_IDENTITY_PREFIX + ipAddress);
    }

    private void enforceRateLimit(String identityHash) {
        if (!loginRateLimitService.allow(identityHash)) {
            throw new LoginRateLimitExceededException();
        }
    }

    private void resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) {
        exceptionResolver.resolveException(
                request,
                response,
                null,
                exception
        );
    }
}
