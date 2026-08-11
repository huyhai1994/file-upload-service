package org.mini_lab.file_upload_service.file_upload.shared.component;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.shared.configuration.MimeTypePropertiesConfiguration;
import org.mini_lab.file_upload_service.file_upload.upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InvalidMimeTypeException;
import org.mini_lab.file_upload_service.file_upload.upload.service.extractor.MimeTypeExtractor;
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
