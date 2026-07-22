package org.mini_lab.file_upload_service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.TestClockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestClockConfiguration.class)
class FileMetadataRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void markDeletingIfCompleted_whenFileStateIsCompleted_thenReturnOne() {
        FileMetadata fileMetadata = getValidCompletedFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, fileMetadataRepository.markDeletingIfCompleted(fileId));
    }


    @Test
    void markDeletingIfCompleted_whenFileStateNotCompleted_thenReturnZero() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, fileMetadataRepository.markDeletingIfCompleted(fileId));
    }

    @Test
    void markFailedIfUploading_whenUploadingFailed_changeStateFromUploadingToFailed() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, fileMetadataRepository.markFailedIfUploading(fileId));

    }

    @Test
    void markFailedIfUploading_whenUploadingCompleted_updateRowIsZero() {
        FileMetadata fileMetadata = getValidCompletedFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        String checksum = "fake-checksum";
        entityManager.flush();
        entityManager.clear();
        assertEquals(0, fileMetadataRepository.markCompletedIfUploading(fileId, checksum));
    }

    @Test
    void markCompletedIfUploading_whenFileIsUploading_changeStateToCompleted() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        String checksum = "fake-checksum";
        assertEquals(1, fileMetadataRepository.markCompletedIfUploading(fileId, checksum));
    }

    @Test
    void markCompletedIfUploading_whenUploadingCompleted_updatedRowIsZero() {
        FileMetadata fileMetadata = getValidCompletedFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        String checksum = "fake-checksum";
        assertEquals(0, fileMetadataRepository.markCompletedIfUploading(fileId, checksum));
    }


    @Test
    void save_whenSaveFileMetadata_thenReturnPersistedFileMetadata() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();

        FileMetadata persistedFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        assertEquals("Avatar of John Doe", persistedFileMetadata.getTitle());
        assertNotNull(persistedFileMetadata.getCreatedAt());
    }

    @Test
    void save_whenMissingNonNullField_thenThrowException() {
        assertThrows(DataIntegrityViolationException.class, () -> fileMetadataRepository.saveAndFlush(getNonValidUploadingFileMetadata()));
    }

    @Test
    void getFileMetadataById_whenFileMetadataExists_thenOptionalNotEmpty() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();

        FileMetadata persistedFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        entityManager.flush();
        entityManager.clear();

        assertThat(fileMetadataRepository.getFileMetadataById(persistedFileMetadata.getId())).isNotEmpty();

    }

    @Test
    void getFileMetadataById_whenFileMetadataNotExits_thenOptionalIsEmpty() {
        assertThat(fileMetadataRepository.getFileMetadataById(1L)).isEmpty();
    }


}