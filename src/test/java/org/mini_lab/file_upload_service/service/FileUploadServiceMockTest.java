package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.exception.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceMockTest {

    @InjectMocks
    FileUploadService fileUploadService;

    @Mock
    FileUploadRequestExtractor fileUploadRequestExtractor;

    @Mock
    FileRequestVerifyService fileRequestVerifyService;

    @Mock
    FileMetadataCreationService fileMetadataCreationService;

    @Mock
    FileUploadStateManager fileUploadStateManager;

    @Mock
    ObjectStorageClient objectStorageClient;

    private FileUploadCommand command;

    private FileMetadata fileMetadata;

    private UploadRequestObjectDTO uploadRequestObjectDTO;

    @BeforeEach
    void setUp() {
        MockMultipartFile file = getTextContentTypeMultipartFile();
        command = getFileUploadCommand(file);
        fileMetadata = getValidUploadingFileMetadata();
        fileMetadata.setId(1L);
        uploadRequestObjectDTO = new UploadRequestObjectDTO(file, "mock title");
    }

    @Test
    void whenUploadAndWriteMetadataSuccess_thenMarkCompleted() {

        when(fileUploadRequestExtractor.extract(any(UploadRequestObjectDTO.class)))
                .thenReturn(command);
        when(fileMetadataCreationService.createUploadingMetadata(any(FileUploadCommand.class))).thenReturn(fileMetadata);

        when(objectStorageClient.upload(any(String.class), any(FileUploadCommand.class))).thenReturn(getUploadObjectResult());

        fileUploadService.processUploadFile(uploadRequestObjectDTO);

        verify(fileUploadStateManager).markCompleted(eq(1L), any(String.class));

    }

    @Test
    void whenFileIsEmpty_thenThrowExceptionAndShouldNotUpload() {
        UploadRequestObjectDTO request =
                new UploadRequestObjectDTO(getTextContentTypeMultipartFile(), "mock title");

        when(fileUploadRequestExtractor.extract(any(UploadRequestObjectDTO.class)))
                .thenReturn(command);

        doThrow(new EmptyFileException())
                .when(fileRequestVerifyService)
                .validate(command);

        assertThrows(
                EmptyFileException.class,
                () -> fileUploadService.processUploadFile(request)
        );

        verify(fileRequestVerifyService).validate(command);

        verifyNoInteractions(
                fileMetadataCreationService,
                objectStorageClient,
                fileUploadStateManager
        );
    }

    @Test
    void whenFileExtensionInvalid_thenThrowInvalidFileExtensionExceptionAndShouldNotUpload() {

        UploadRequestObjectDTO request =
                new UploadRequestObjectDTO(getTextContentTypeMultipartFile(), "title");

        when(fileUploadRequestExtractor.extract(any(UploadRequestObjectDTO.class)))
                .thenReturn(command);

        doThrow(new InvalidFileExtensionException())
                .when(fileRequestVerifyService)
                .validate(command);

        assertThrows(
                InvalidFileExtensionException.class,
                () -> fileUploadService.processUploadFile(request)
        );

        verify(fileRequestVerifyService).validate(command);

        verifyNoInteractions(
                fileMetadataCreationService,
                objectStorageClient,
                fileUploadStateManager
        );
    }

    @Test
    void whenMimeFileInvalid_thenThrowInvalidMimeTypeExceptionAndShouldNotUpload() {

        UploadRequestObjectDTO request =
                new UploadRequestObjectDTO(getTextContentTypeMultipartFile(), "title");

        when(fileUploadRequestExtractor.extract(any(UploadRequestObjectDTO.class)))
                .thenReturn(command);

        doThrow(new InvalidMimeTypeException())
                .when(fileRequestVerifyService)
                .validate(command);

        assertThrows(
                InvalidMimeTypeException.class,
                () -> fileUploadService.processUploadFile(request)
        );

        verify(fileRequestVerifyService).validate(command);

        verifyNoInteractions(
                fileMetadataCreationService,
                objectStorageClient,
                fileUploadStateManager
        );
    }

    @Test
    void whenFileNameNotValid_thenThrowInvalidFilenameExceptionAndShouldNotUpload() {

        UploadRequestObjectDTO request =
                new UploadRequestObjectDTO(getTextContentTypeMultipartFile(), "mock title");

        when(fileUploadRequestExtractor.extract(any(UploadRequestObjectDTO.class)))
                .thenReturn(command);

        doThrow(new InvalidFilenameException())
                .when(fileRequestVerifyService)
                .validate(command);

        assertThrows(
                InvalidFilenameException.class,
                () -> fileUploadService.processUploadFile(request)
        );

        verify(fileRequestVerifyService).validate(command);

        verifyNoInteractions(
                fileMetadataCreationService,
                objectStorageClient,
                fileUploadStateManager
        );
    }

    @Test
    void whenUploadFails_thenShouldNotMarkCompletedAndShouldHandleFailure() {
        UploadRequestObjectDTO request =
                new UploadRequestObjectDTO(getTextContentTypeMultipartFile(), "title");

        when(fileUploadRequestExtractor.extract(any(UploadRequestObjectDTO.class)))
                .thenReturn(command);

        when(fileMetadataCreationService.createUploadingMetadata(command))
                .thenReturn(fileMetadata);

        when(objectStorageClient.upload(
                fileMetadata.getObjectKey(),
                command
        )).thenThrow(new ObjectStorageException());

        assertThrows(
                InternalServerException.class,
                () -> fileUploadService.processUploadFile(request)
        );

        verify(fileUploadRequestExtractor).extract(request);
        verify(fileRequestVerifyService).validate(command);

        verify(fileMetadataCreationService)
                .createUploadingMetadata(command);

        verify(objectStorageClient)
                .upload(fileMetadata.getObjectKey(), command);

        verify(objectStorageClient)
                .delete(fileMetadata.getObjectKey());

        verify(fileUploadStateManager)
                .markFailed(fileMetadata.getId());

        verify(fileUploadStateManager, never())
                .markCompleted(anyLong(), anyString());
    }

}