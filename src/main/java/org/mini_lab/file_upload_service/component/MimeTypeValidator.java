package org.mini_lab.file_upload_service.component;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.configuration.MimeTypePropertiesConfiguration;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.InvalidMimeTypeException;
import org.mini_lab.file_upload_service.service.extractor.MimeTypeExtractor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MimeTypeValidator implements FileValidator {
    private final MimeTypeExtractor mimeTypeExtractor;
    private final MimeTypePropertiesConfiguration mimeTypePropertiesConfiguration;

    @Override
    public int order() {
        return 40;
    }

    @Override
    public void validate(FileUploadCommand command) {
        String mimeType = mimeTypeExtractor.extract(command.file());
        if (!Objects.equals(command.file().getContentType(), mimeType)) {
            throw new InvalidMimeTypeException();
        }
        if (!mimeTypePropertiesConfiguration.getAllowedList().contains(mimeType)) {
            throw new InvalidMimeTypeException();
        }
    }
}
