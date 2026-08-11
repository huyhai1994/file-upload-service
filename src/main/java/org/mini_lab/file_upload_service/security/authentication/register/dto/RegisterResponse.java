package org.mini_lab.file_upload_service.security.authentication.register.dto;

import java.util.UUID;

public record RegisterResponse(UUID id, String username) {
}
