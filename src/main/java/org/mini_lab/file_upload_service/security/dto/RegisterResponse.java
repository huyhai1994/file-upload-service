package org.mini_lab.file_upload_service.security.dto;

import java.util.UUID;

public record RegisterResponse(UUID id, String username) {
}
