package org.mini_lab.file_upload_service.service.upload;

import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.file_upload.UploadRequestObjectDTO;
import org.springframework.stereotype.Service;

@Service
public class FileUploadRequestExtractor {
    public FileUploadCommand extract(UploadRequestObjectDTO request) {
        return FileUploadCommand.builder()
                .file(request.file())
                .originalFileName(request.file().getOriginalFilename())
                .contentType(request.file().getContentType())
                .size(request.file().getSize())
                .title(request.title())
                .build();
    }
}
