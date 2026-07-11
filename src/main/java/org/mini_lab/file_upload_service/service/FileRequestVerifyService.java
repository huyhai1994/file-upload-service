package org.mini_lab.file_upload_service.service;

import org.mini_lab.file_upload_service.component.FileValidator;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FileRequestVerifyService {

    private final List<FileValidator> fileValidators;

    public FileRequestVerifyService(List<FileValidator> fileValidators) {
        fileValidators.sort(Comparator.comparingInt(FileValidator::order));
        this.fileValidators = fileValidators;
    }

    private void validate(FileUploadCommand command) {
        fileValidators.forEach(fileValidator -> fileValidator.validate(command));
    }


}
