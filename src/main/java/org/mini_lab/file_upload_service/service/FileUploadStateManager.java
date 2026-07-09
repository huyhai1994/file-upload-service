package org.mini_lab.file_upload_service.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileUploadStateManager {
    private final FileMetadataRepository fileMetadataRepository;

    @Transactional
    public void markCompleted(Long fileId, String checkSum) {


    }
}
