package org.mini_lab.file_upload_service.component;

import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.file_upload.InvalidFilenameException;
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
