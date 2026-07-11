package org.mini_lab.file_upload_service.component;

import org.mini_lab.file_upload_service.dto.FileUploadCommand;

public interface FileValidator {

    int order();

    void validate(FileUploadCommand command);
}
