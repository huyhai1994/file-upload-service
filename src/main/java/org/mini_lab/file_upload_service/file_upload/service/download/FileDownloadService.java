package org.mini_lab.file_upload_service.file_upload.service.download;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.dto.file_upload.FileDownloadResource;
import org.mini_lab.file_upload_service.file_upload.entity.FileMetadata;
import org.mini_lab.file_upload_service.file_upload.entity.FileState;
import org.mini_lab.file_upload_service.file_upload.exception.FileNotAvailableException;
import org.mini_lab.file_upload_service.file_upload.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.file_upload.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.file_upload.service.s3.ObjectStorageClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final FileMetadataRepository fileMetadataRepository;

    private final ObjectStorageClient objectStorageClient;

    @Transactional(readOnly = true)
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
