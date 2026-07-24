package org.mini_lab.file_upload_service.service.validator;

import org.mini_lab.file_upload_service.component.FileValidator;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.file_upload.FileNotAvailableException;
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

    public void validate(FileUploadCommand command) {
        fileValidators.forEach(fileValidator -> fileValidator.validate(command));
    }

    public void verifyFileAvailable(Long fileId, FileState expectedState, FileState currentState) {
        if (expectedState != currentState) {
            throw new FileNotAvailableException(fileId, expectedState);
        }
    }


}
