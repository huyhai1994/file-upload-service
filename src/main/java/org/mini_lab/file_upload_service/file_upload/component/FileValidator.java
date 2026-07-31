package org.mini_lab.file_upload_service.file_upload.component;

import org.mini_lab.file_upload_service.file_upload.dto.file_upload.FileUploadCommand;

public interface FileValidator {

    int order();

    void validate(FileUploadCommand command);
}
