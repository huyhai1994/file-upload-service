package org.mini_lab.file_upload_service.security.jwt.dto;

import java.util.List;

public record AccessTokenPayload(
        String username,
        List<String> authorities
) {
}