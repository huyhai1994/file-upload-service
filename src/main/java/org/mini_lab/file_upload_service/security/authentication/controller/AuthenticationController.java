package org.mini_lab.file_upload_service.security.authentication.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.shared.response.ApiResponse;
import org.mini_lab.file_upload_service.security.authentication.dto.LoginRequest;
import org.mini_lab.file_upload_service.security.authentication.dto.LoginResponse;
import org.mini_lab.file_upload_service.security.authentication.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.dto.RegisterResponse;
import org.mini_lab.file_upload_service.security.authentication.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("register")
    ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid
            @RequestBody
            RegisterRequest request) {
        RegisterResponse response = authenticationService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("login")
    @WithSpan("authentication-controller-login")
    ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid
            @RequestBody
            LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
