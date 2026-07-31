package org.mini_lab.file_upload_service.file_upload.shared.component;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.configuration.ExtensionPropertiesConfigurations;
import org.mini_lab.file_upload_service.file_upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.exception.InvalidFileExtensionException;
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
