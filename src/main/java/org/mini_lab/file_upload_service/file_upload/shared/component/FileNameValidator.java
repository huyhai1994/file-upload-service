package org.mini_lab.file_upload_service.file_upload.shared.component;

import org.mini_lab.file_upload_service.file_upload.upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InvalidFilenameException;
import org.springframework.stereotype.Component;

@Component
public class FileNameValidator implements FileValidator {
    @Override
    public int order() {
        return 20;
    }

    @Override
    public void validate(FileUploadCommand command) {
        String fileName = command.originalFileName();
        fileNameNotNull(fileName);
    }

    private void fileNameNotNull(String fileName) {
        if (fileName == null || fileName.isBlank())
            throw new InvalidFilenameException();
    }
}
