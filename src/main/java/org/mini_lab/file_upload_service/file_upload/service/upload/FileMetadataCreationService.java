package org.mini_lab.file_upload_service.file_upload.service.upload;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.component.ExtensionExtractor;
import org.mini_lab.file_upload_service.file_upload.component.ObjectKeyGenerator;
import org.mini_lab.file_upload_service.file_upload.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.entity.FileMetadata;
import org.mini_lab.file_upload_service.file_upload.entity.FileState;
import org.mini_lab.file_upload_service.file_upload.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileMetadataCreationService {
    private final FileMetadataRepository fileMetadataRepository;
    private final ObjectKeyGenerator objectKeyGenerator;
    private final ExtensionExtractor extensionExtractor;
    private final Clock clock;

    @Transactional
    @WithSpan("create-uploading-metadata")
    public FileMetadata createUploadingMetadata(FileUploadCommand command) {

        String fileName = command.originalFileName();
        String contentType = command.contentType();
        String objectKey = objectKeyGenerator.generate();
        Long size = command.size();
        FileState fileState = FileState.UPLOADING;
        String title = command.title();
        Optional<String> extension = extensionExtractor.extract(fileName);

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(fileName);
        fileMetadata.setContentType(contentType);
        fileMetadata.setObjectKey(objectKey);
        fileMetadata.setSize(size);
        fileMetadata.setStatus(fileState);
        fileMetadata.setTitle(title);
        fileMetadata.setUploadingAt(LocalDateTime.now(clock));
        fileMetadata.setExtension(extension.orElse(null));

        return fileMetadataRepository.save(fileMetadata);
    }
}
