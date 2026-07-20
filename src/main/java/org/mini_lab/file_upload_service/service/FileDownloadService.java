package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.dto.FileDownloadResource;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.FileNotAvailableException;
import org.mini_lab.file_upload_service.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileMetadataRepository fileMetadataRepository;

    private final ObjectStorageClient objectStorageClient;

    public FileDownloadResource prepareDownload(Long fileId) {
        FileMetadata metadata = fileMetadataRepository.getFileMetadataById(fileId).orElseThrow(FileNotFoundException::new);

        validateDownloadable(metadata);

        return buildFileDownloadResource(metadata);
    }

    private FileDownloadResource buildFileDownloadResource(FileMetadata metadata) {
        return FileDownloadResource.builder()
                .contentType(metadata.getContentType())
                .fileName(metadata.getFileName())
                .size(metadata.getSize())
                .inputStreamSupplier(() -> objectStorageClient.getObject(metadata.getObjectKey()))
                .build();
    }

    private void validateDownloadable(FileMetadata metadata) {
        if (!Objects.equals(FileState.COMPLETED, metadata.getStatus())) {
            throw new FileNotAvailableException(
                    metadata.getId(),
                    metadata.getStatus()
            );
        }
    }


}
