package org.mini_lab.file_upload_service.file_upload.upload.dto;

import lombok.Builder;

@Builder
public record UploadObjectResult(
        String etag,
        String checksum
) {
}
