package org.mini_lab.file_upload_service.security.rate_limiter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.security.rate_limiter.component.IPAddressDetector;
import org.mini_lab.file_upload_service.security.rate_limiter.component.IdentityHasher;
import org.mini_lab.file_upload_service.security.rate_limiter.exceptions.LoginRateLimitExceededException;
import org.mini_lab.file_upload_service.security.rate_limiter.service.LoginRateLimitService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.mini_lab.file_upload_service.support.MockUserBuilder.HASHED_IP_ADDRESS;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.IP_ADDRESS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginRateLimiterFilterMockTest {

    @Mock
    IPAddressDetector ipAddressDetector;

    @Mock
    IdentityHasher identityHasher;

    @Mock
    HandlerExceptionResolver exceptionResolver;

    @Mock
    LoginRateLimitService loginRateLimitService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @InjectMocks
    LoginRateLimiterFilter loginRateLimiterFilter;

    @Test
    void doFilterInternal_whenLoginRequestExceedsLimit_thenShouldNotContinueFilterChain()
            throws ServletException, IOException {

        String rawIdentity = "IP|" + IP_ADDRESS;

        when(ipAddressDetector.detect(request))
                .thenReturn(IP_ADDRESS);

        when(identityHasher.hash(rawIdentity))
                .thenReturn(HASHED_IP_ADDRESS);

        when(loginRateLimitService.allow(HASHED_IP_ADDRESS))
                .thenReturn(false);

        loginRateLimiterFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(ipAddressDetector).detect(request);
        verify(identityHasher).hash(rawIdentity);
        verify(loginRateLimitService).allow(HASHED_IP_ADDRESS);

        verify(exceptionResolver).resolveException(
                eq(request),
                eq(response),
                isNull(),
                any(LoginRateLimitExceededException.class)
        );

        verify(filterChain, never())
                .doFilter(any(), any());
    }

    @Test
    void doFilterInternal_whenLoginRequestNotExceedsLimit_thenShouldContinueFilterChain()
            throws ServletException, IOException {

        String rawIdentity = "IP|" + IP_ADDRESS;

        when(ipAddressDetector.detect(request))
                .thenReturn(IP_ADDRESS);

        when(identityHasher.hash(rawIdentity))
                .thenReturn(HASHED_IP_ADDRESS);

        when(loginRateLimitService.allow(HASHED_IP_ADDRESS))
                .thenReturn(true);

        loginRateLimiterFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(ipAddressDetector).detect(request);
        verify(identityHasher).hash(rawIdentity);
        verify(loginRateLimitService).allow(HASHED_IP_ADDRESS);

        verify(filterChain)
                .doFilter(any(), any());
    }
}