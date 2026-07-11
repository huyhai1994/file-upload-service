package org.mini_lab.file_upload_service.component;

import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.EmptyFileException;
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
