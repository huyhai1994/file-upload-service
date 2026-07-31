package org.mini_lab.file_upload_service.file_upload.dto;

import lombok.Builder;
import org.mini_lab.file_upload_service.file_upload.component.InputStreamSupplier;

@Builder
public record FileDownloadResource(
        String fileName,
        Long size,
        String contentType,
        InputStreamSupplier inputStreamSupplier
) {
}
