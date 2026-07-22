package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.dto.FileDownloadResource;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.exception.FileNotAvailableException;
import org.mini_lab.file_upload_service.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceMockTest {
    @InjectMocks
    FileDownloadService fileDownloadService;

    @Mock
    FileMetadataRepository fileMetadataRepository;

    @Mock
    ObjectStorageClient objectStorageClient;

    @Test
    void prepareDownload_whenMetadataDoesNotExist_thenThrowFileNotFoundException() {
        Long fileId = 1L;

        when(fileMetadataRepository.getFileMetadataById(fileId))
                .thenReturn(Optional.empty());

        assertThrows(
                FileNotFoundException.class,
                () -> fileDownloadService.prepareDownload(fileId)
        );

        verify(fileMetadataRepository).getFileMetadataById(fileId);
        verifyNoInteractions(objectStorageClient);
    }

    @Test
    void prepareDownload_whenStateIsNotCompleted_thenThrowFileNotAvailableException() {
        FileMetadata fileMetadata = MockObjectBuilder.getNonValidUploadingFileMetadata();
        Long id = 1L;
        fileMetadata.setId(1L);
        when(fileMetadataRepository.getFileMetadataById(id)).thenReturn(Optional.of(fileMetadata));

        assertThrows(FileNotAvailableException.class, () -> fileDownloadService.prepareDownload(id));

        verify(fileMetadataRepository).getFileMetadataById(id);

        verifyNoInteractions(objectStorageClient);

    }

    @Test
    void prepareDownload_whenFileIsReady_thenReturnFileDownloadResource() {
        FileMetadata fileMetadata = MockObjectBuilder.getValidCompletedFileMetadata();
        Long id = 1L;
        fileMetadata.setId(1L);
        when(fileMetadataRepository.getFileMetadataById(id)).thenReturn(Optional.of(fileMetadata));

        FileDownloadResource resource = fileDownloadService.prepareDownload(id);

        verify(fileMetadataRepository).getFileMetadataById(id);
        assertEquals(fileMetadata.getContentType(), resource.contentType());
        assertEquals(fileMetadata.getFileName(), resource.fileName());
        assertEquals(fileMetadata.getSize(), resource.size());
        assertNotNull(resource.inputStreamSupplier());
    }



}

