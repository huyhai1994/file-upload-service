package org.mini_lab.file_upload_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface MimeTypeExtractor {
    String extract(MultipartFile file);
}
