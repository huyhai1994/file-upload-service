package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.FileNotAvailableException;
import org.mini_lab.file_upload_service.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDeleteServiceMockTest {

    @InjectMocks
    FileDeleteService fileDeleteService;

    @Mock
    FileMetadataRepository fileMetadataRepository;

    @Mock
    ObjectStorageClient objectStorageClient;

    @Mock
    FileMetadataStateManager fileMetadataStateManager;

    @Mock
    FileVerifyService fileVerifyService;

    @Test
    void processDeleteFile_whenDeleteSuccess_thenReturnFileDeleteResponseDTO() {
        Long fileId = 1L;
        FileMetadata fileMetadata = MockObjectBuilder.getValidCompletedFileMetadata();
        fileMetadata.setId(fileId);
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(fileMetadata));

        fileDeleteService.processDeleteFile(fileId);

        verify(fileVerifyService).verifyFileAvailable(eq(fileId), any(FileState.class), any(FileState.class));
        verify(fileMetadataStateManager).markDeleting(eq(fileId));
        verify(objectStorageClient).delete(anyString());
        verify(fileMetadataStateManager).markDeleted(eq(fileId));
    }


    @Test
    void processDeleteFile_whenFileMetadataNotFound_thenThrowsFileNotFoundException() {
        Long fileId = 1L;
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> fileDeleteService.processDeleteFile(fileId));

        verify(fileVerifyService, never()).verifyFileAvailable(eq(fileId), any(FileState.class), any(FileState.class));
        verifyNoInteractions(fileMetadataStateManager, objectStorageClient);
    }

    @Test
    void processDeleteFile_whenFileNotAvailable_thenThrowsFileNotAvailableException() {
        Long fileId = 1L;

        FileMetadata fileMetadata =
                MockObjectBuilder.getValidCompletedFileMetadata();

        fileMetadata.setId(fileId);
        fileMetadata.setStatus(FileState.FAILED);

        when(fileMetadataRepository.findById(fileId))
                .thenReturn(Optional.of(fileMetadata));

        doThrow(new FileNotAvailableException(
                fileId,
                FileState.COMPLETED
        ))
                .when(fileVerifyService)
                .verifyFileAvailable(
                        fileId,
                        FileState.COMPLETED,
                        FileState.FAILED
                );

        assertThrows(
                FileNotAvailableException.class,
                () -> fileDeleteService.processDeleteFile(fileId)
        );

        verify(fileMetadataRepository).findById(fileId);

        verify(fileVerifyService).verifyFileAvailable(
                fileId,
                FileState.COMPLETED,
                FileState.FAILED
        );

        verifyNoInteractions(
                fileMetadataStateManager,
                objectStorageClient
        );
    }
}