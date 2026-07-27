package org.mini_lab.file_upload_service.component;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.configuration.ExtensionPropertiesConfigurations;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.file_upload.InvalidFileExtensionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExtensionValidator implements FileValidator {

    private final ExtensionPropertiesConfigurations extensionPropertiesConfigurations;

    private final ExtensionExtractor extensionExtractor;

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void validate(FileUploadCommand command) {
        List<String> allowedList = extensionPropertiesConfigurations.getAllowedList();
        String fileName = command.originalFileName();
        String extension = extensionExtractor.extract(fileName).orElseThrow(InvalidFileExtensionException::new);
        if (!allowedList.contains(extension))
            throw new InvalidFileExtensionException();
    }
}
