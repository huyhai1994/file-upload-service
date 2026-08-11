package org.mini_lab.file_upload_service.security.authentication.register.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.shared.response.ApiResponse;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterResponse;
import org.mini_lab.file_upload_service.security.authentication.register.service.UserAccountRegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class RegisterController {

    private final UserAccountRegisterService userAccountRegisterService;

    @PostMapping("register")
    ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid
            @RequestBody
            RegisterRequest request) {
        RegisterResponse response = userAccountRegisterService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

}
