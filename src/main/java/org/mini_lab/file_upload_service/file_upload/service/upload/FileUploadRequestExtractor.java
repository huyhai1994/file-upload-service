package org.mini_lab.file_upload_service.file_upload.service.upload;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.mini_lab.file_upload_service.file_upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.dto.UploadRequestObjectDTO;
import org.springframework.stereotype.Service;

@Service
public class FileUploadRequestExtractor {
    @WithSpan("request-extract")
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
