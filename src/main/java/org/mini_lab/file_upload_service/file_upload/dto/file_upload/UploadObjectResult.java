package org.mini_lab.file_upload_service.file_upload.dto.file_upload;

import lombok.Builder;

@Builder
public record UploadObjectResult(
        String etag,
        String checksum
) {
}
