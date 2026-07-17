package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.component.ObjectKeyGenerator;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileMetadataCreationService {
    private final FileMetadataRepository fileMetadataRepository;
    private final ObjectKeyGenerator objectKeyGenerator;

    @Transactional
    public FileMetadata createUploadingMetadata(FileUploadCommand command) {

        String fileName = command.originalFileName();
        String contentType = command.contentType();
        String objectKey = objectKeyGenerator.generate();
        Long size = command.size();
        FileState fileState = FileState.UPLOADING;
        String title = command.title();

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(fileName);
        fileMetadata.setContentType(contentType);
        fileMetadata.setObjectKey(objectKey);
        fileMetadata.setSize(size);
        fileMetadata.setStatus(fileState);
        fileMetadata.setTitle(title);

        return fileMetadataRepository.save(fileMetadata);
    }
}
