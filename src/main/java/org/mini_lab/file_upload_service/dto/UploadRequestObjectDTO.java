package org.mini_lab.file_upload_service.dto;

import org.springframework.web.multipart.MultipartFile;

public record UploadRequestObjectDTO(MultipartFile file) {
}
