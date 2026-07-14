package org.mini_lab.file_upload_service.component;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.configuration.ExtensionPropertiesConfigurations;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.InvalidFileExtensionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExtensionValidator implements FileValidator {

    private final ExtensionPropertiesConfigurations extensionPropertiesConfigurations;


    @Override
    public int order() {
        return 30;
    }

    @Override
    public void validate(FileUploadCommand command) {
        List<String> allowedList = extensionPropertiesConfigurations.getAllowedList();
        String fileName = command.originalFileName();
        String extension = null;
        if (fileName != null) {
            extension = fileName
                    .substring(fileName.lastIndexOf('.') + 1);
        }
        if (extension == null)
            throw new InvalidFileExtensionException();
        if (!allowedList.contains(extension))
            throw new InvalidFileExtensionException();
    }
}
