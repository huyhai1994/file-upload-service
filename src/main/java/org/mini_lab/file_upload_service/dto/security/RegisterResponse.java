package org.mini_lab.file_upload_service.dto.security;

import java.util.UUID;

public record RegisterResponse(UUID id, String username) {
}
