package org.mini_lab.file_upload_service.security.jwt.dto;

import java.util.List;

public record AuthenticatedUser(
        String username,
        List<String> authorities
) {
}
