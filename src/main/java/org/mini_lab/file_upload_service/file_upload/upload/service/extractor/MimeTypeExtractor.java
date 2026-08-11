package org.mini_lab.file_upload_service.file_upload.upload.service.extractor;

import org.springframework.web.multipart.MultipartFile;

public interface MimeTypeExtractor {
    String extract(MultipartFile file);
}
