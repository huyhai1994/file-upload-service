package org.mini_lab.file_upload_service.security.rate_limiter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mini_lab.file_upload_service.security.rate_limiter.component.IPAddressDetector;
import org.mini_lab.file_upload_service.security.rate_limiter.component.IdentityHasher;
import org.mini_lab.file_upload_service.security.rate_limiter.exceptions.LoginRateLimitExceededException;
import org.mini_lab.file_upload_service.security.rate_limiter.service.LoginRateLimitService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class LoginRateLimiterFilter extends OncePerRequestFilter {

    private final LoginRateLimitService loginRateLimitService;
    private final IPAddressDetector ipAddressDetector;
    private final IdentityHasher identityHasher;
    private final HandlerExceptionResolver exceptionResolver;

    public LoginRateLimiterFilter(
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
        return !(
                request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())
                        && request.getRequestURI().equals("/api/v1/auth/login")
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ipAddress = ipAddressDetector.detect(request);

        String identityHash = identityHasher.hash(
                "IP|" + ipAddress
        );

        if (!loginRateLimitService.allow(identityHash)) {
            exceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new LoginRateLimitExceededException()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
