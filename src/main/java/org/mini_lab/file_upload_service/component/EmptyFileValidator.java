package org.mini_lab.file_upload_service.component;

import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.file_upload.EmptyFileException;
import org.springframework.stereotype.Component;

@Component
public class EmptyFileValidator implements FileValidator {
    @Override
    public int order() {
        return 10;
    }

    @Override
    public void validate(FileUploadCommand command) {
        if(command.file() == null || command.file().isEmpty())
            throw new EmptyFileException();
    }
}
