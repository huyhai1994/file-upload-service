package org.mini_lab.file_upload_service.file_upload.shared.component;

import org.mini_lab.file_upload_service.file_upload.dto.FileUploadCommand;

public interface FileValidator {

    int order();

    void validate(FileUploadCommand command);
}
