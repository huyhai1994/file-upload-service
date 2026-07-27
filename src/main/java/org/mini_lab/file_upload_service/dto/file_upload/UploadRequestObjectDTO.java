package org.mini_lab.file_upload_service.dto.file_upload;

import org.springframework.web.multipart.MultipartFile;

public record UploadRequestObjectDTO(MultipartFile file, String title) {
}
