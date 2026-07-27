package org.mini_lab.file_upload_service.dto.file_upload;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record FileUploadCommand(
        MultipartFile file,
        String originalFileName,
        String contentType,
        Long size,
        String title
) {
}
