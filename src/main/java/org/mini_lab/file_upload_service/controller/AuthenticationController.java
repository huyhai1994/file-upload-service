package org.mini_lab.file_upload_service.controller;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.dto.security.LoginRequest;
import org.mini_lab.file_upload_service.dto.security.LoginResponse;
import org.mini_lab.file_upload_service.dto.security.RegisterRequest;
import org.mini_lab.file_upload_service.dto.security.RegisterResponse;
import org.mini_lab.file_upload_service.service.security.AuthenticationService;
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
