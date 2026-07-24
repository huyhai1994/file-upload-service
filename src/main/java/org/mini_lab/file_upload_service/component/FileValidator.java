package org.mini_lab.file_upload_service.component;

import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;

public interface FileValidator {

    int order();

    void validate(FileUploadCommand command);
}
