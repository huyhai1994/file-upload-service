package org.mini_lab.file_upload_service.dto;

import lombok.Builder;
import org.mini_lab.file_upload_service.component.InputStreamSupplier;

@Builder
public record FileDownloadResource(
        String fileName,
        Long size,
        String contentType,
        InputStreamSupplier inputStreamSupplier
) {
}
