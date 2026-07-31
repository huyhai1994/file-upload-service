package org.mini_lab.file_upload_service.security.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record RegisterRequest(@NonNull @NotBlank String username,
                              @NonNull @NotBlank String password) {

}
