package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.component.ExtensionExtractor;
import org.mini_lab.file_upload_service.component.ObjectKeyGenerator;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getFileUploadCommand;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileMetadataCreationServiceMockTest {

    private static final String OBJECT_KEY =
            "files/2026/06/550e8400-e29b-41d4-a716-446655440000";

    @InjectMocks
    private FileMetadataCreationService fileMetadataCreationService;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private ObjectKeyGenerator objectKeyGenerator;

    @Mock
    private ExtensionExtractor extensionExtractor;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-07-08T10:00:00Z"),
                ZoneOffset.UTC
        );

        fileMetadataCreationService =
                new FileMetadataCreationService(
                        fileMetadataRepository,
                        objectKeyGenerator,
                        extensionExtractor,
                        clock
                );
    }

    @Test
    void whenCreateUploadingMetadata_thenGenerateObjectKeyAndSaveMetadata() {
        // Given
        FileUploadCommand command = getFileUploadCommand(getTextContentTypeMultipartFile());

        when(objectKeyGenerator.generate()).thenReturn(OBJECT_KEY);
        when(extensionExtractor.extract(anyString())).thenReturn(Optional.of("txt"));

        // When
        fileMetadataCreationService.createUploadingMetadata(command);

        // Then
        ArgumentCaptor<FileMetadata> metadataCaptor =
                ArgumentCaptor.forClass(FileMetadata.class);

        verify(objectKeyGenerator).generate();
        verify(fileMetadataRepository).save(metadataCaptor.capture());

        FileMetadata savedMetadata = metadataCaptor.getValue();

        assertEquals(OBJECT_KEY, savedMetadata.getObjectKey());
        assertEquals("test.txt", savedMetadata.getFileName());
        assertEquals("text/plain", savedMetadata.getContentType());
        assertEquals(11L, savedMetadata.getSize());
        assertEquals(FileState.UPLOADING, savedMetadata.getStatus());
        assertNotNull(savedMetadata.getUploadingAt());
    }

}