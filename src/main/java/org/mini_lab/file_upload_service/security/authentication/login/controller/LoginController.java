package org.mini_lab.file_upload_service.security.authentication.login.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginRequest;
import org.mini_lab.file_upload_service.security.authentication.login.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.authentication.login.service.UserAccountLoginService;
import org.mini_lab.file_upload_service.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {
    private final UserAccountLoginService userAccountLoginService;

    @PostMapping("login")
    @WithSpan("login-controller-login")
    ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid
            @RequestBody
            LoginRequest request) {
        LoginResponse response = userAccountLoginService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
