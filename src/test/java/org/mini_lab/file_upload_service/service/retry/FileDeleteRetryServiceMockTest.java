package org.mini_lab.file_upload_service.service.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.exception.FileNotFoundException;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.s3.ObjectStorageClient;
import org.mini_lab.file_upload_service.service.state_manager.FileMetadataStateManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDeleteRetryServiceMockTest {
    private static final Long FILE_ID = 1L;
    private static final String OBJECT_KEY = "files/2026/07/test-object-key";

    @Mock
    private ObjectStorageClient objectStorageClient;

    @Mock
    private FileMetadataStateManager fileMetadataStateManager;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private FileDeleteRetryService fileDeleteRetryService;

    private FileMetadata fileMetadata;

    @BeforeEach
    void setUp() {
        fileMetadata = mock(FileMetadata.class);
    }

    @Test
    void retryTimedOutFile_whenFileExistsAndObjectDeleteSucceeds_thenMarkFileDeleted() {
        // given
        when(fileMetadataRepository.findById(FILE_ID))
                .thenReturn(Optional.of(fileMetadata));

        when(fileMetadata.getObjectKey())
                .thenReturn(OBJECT_KEY);

        // when
        fileDeleteRetryService.retryTimedOutFile(FILE_ID);

        // then
        verify(fileMetadataRepository).findById(FILE_ID);
        verify(objectStorageClient).delete(OBJECT_KEY);
        verify(fileMetadataStateManager).markDeleted(FILE_ID);

        verifyNoMoreInteractions(
                fileMetadataRepository,
                objectStorageClient,
                fileMetadataStateManager
        );
    }

    @Test
    void retryTimedOutFile_whenObjectStorageDeleteFails_thenDoNotMarkFileDeleted() {
        // given
        when(fileMetadataRepository.findById(FILE_ID))
                .thenReturn(Optional.of(fileMetadata));

        when(fileMetadata.getObjectKey())
                .thenReturn(OBJECT_KEY);

        doThrow(new ObjectStorageException())
                .when(objectStorageClient)
                .delete(OBJECT_KEY);

        // when
        fileDeleteRetryService.retryTimedOutFile(FILE_ID);

        // then
        verify(fileMetadataRepository).findById(FILE_ID);
        verify(objectStorageClient).delete(OBJECT_KEY);
        verify(fileMetadataStateManager, never()).markDeleted(anyLong());
    }

    @Test
    void retryTimedOutFile_whenMetadataDoesNotExist_thenThrowFileNotFoundException() {
        // given
        when(fileMetadataRepository.findById(FILE_ID))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(
                () -> fileDeleteRetryService.retryTimedOutFile(FILE_ID)
        )
                .isInstanceOf(FileNotFoundException.class);

        verify(fileMetadataRepository).findById(FILE_ID);
        verifyNoInteractions(
                objectStorageClient,
                fileMetadataStateManager
        );
    }
}