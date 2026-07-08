package org.mini_lab.file_upload_service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.TestClockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestClockConfiguration.class)
class FileMetadataRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Test
    void save_whenSaveFileMetadata_thenReturnPersistedFileMetadata() {
        FileMetadata fileMetadata = saveValidFileMetadata();

        FileMetadata persistedFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        assertEquals("Avatar of John Doe", persistedFileMetadata.getTitle());
        assertNotNull(persistedFileMetadata.getCreatedAt());
    }

    @Test
    void save_whenMissingNonNullField_thenThrowException() {
        assertThrows(DataIntegrityViolationException.class, () -> fileMetadataRepository.saveAndFlush(saveNonValidFileMetadata()));
    }

    private @NotNull FileMetadata saveValidFileMetadata() {
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setTitle("Avatar of John Doe");
        fileMetadata.setFileName("avatar.png");
        fileMetadata.setContentType("image/png");
        fileMetadata.setExtension("png");
        fileMetadata.setObjectKey("2026/07/08/8b7c3d0d-f0d1-4a7c-a3a4-7d4ef2e81a55.png");
        fileMetadata.setBucket("personal-cloud-storage");
        fileMetadata.setSize(512_384L);
        fileMetadata.setChecksum("9d5ed678fe57bcca610140957afab571f6d9f1f5e53e7d8d0b8f359bd2d96d8e");
        fileMetadata.setStatus(FileState.UPLOADING);
        return fileMetadata;
    }

    private FileMetadata saveNonValidFileMetadata() {
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setTitle("Avatar of John Doe");
        fileMetadata.setFileName(null);
        fileMetadata.setContentType(null);
        fileMetadata.setExtension("png");
        fileMetadata.setObjectKey(null);
        fileMetadata.setBucket(null);
        fileMetadata.setSize(512_384L);
        fileMetadata.setChecksum("9d5ed678fe57bcca610140957afab571f6d9f1f5e53e7d8d0b8f359bd2d96d8e");
        fileMetadata.setStatus(null);
        return fileMetadata;
    }

}