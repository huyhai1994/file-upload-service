package org.mini_lab.file_upload_service.file_upload.service.validator;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.mini_lab.file_upload_service.file_upload.shared.component.FileValidator;
import org.mini_lab.file_upload_service.file_upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.shared.entity.FileState;
import org.mini_lab.file_upload_service.file_upload.exception.FileNotAvailableException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FileVerifyService {

    private final List<FileValidator> fileValidators;

    public FileVerifyService(List<FileValidator> fileValidators) {
        this.fileValidators =
                fileValidators.stream()
                        .sorted(Comparator.comparingInt(FileValidator::order))
                        .toList();
    }

    @WithSpan("request-validate")
    public void validate(FileUploadCommand command) {
        fileValidators.forEach(fileValidator -> fileValidator.validate(command));
    }

    public void verifyFileAvailable(Long fileId, FileState expectedState, FileState currentState) {
        if (expectedState != currentState) {
            throw new FileNotAvailableException(fileId, expectedState);
        }
    }


}
