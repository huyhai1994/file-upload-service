package org.mini_lab.file_upload_service.dto.security;

import lombok.NonNull;

public record RegisterRequest(@NonNull String username, @NonNull String password) {

}
