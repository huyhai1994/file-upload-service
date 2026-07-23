package org.mini_lab.file_upload_service;

import jakarta.persistence.EntityManager;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private Clock clock;

    @Test
    void markDeletingIfCompleted_whenFileStateIsCompleted_thenReturnOne() {
        FileMetadata fileMetadata = getValidCompletedFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, fileMetadataRepository.markDeletingIfCompleted(fileId, LocalDateTime.now(clock)));
    }


    @Test
    void markDeletingIfCompleted_whenFileStateNotCompleted_thenReturnZero() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();

        entityManager.flush();
        entityManager.clear();

        assertEquals(0, fileMetadataRepository.markDeletingIfCompleted(fileId, LocalDateTime.now(clock)));
    }

    @Test
    void markFailedIfUploading_whenUploadingFailed_changeStateFromUploadingToFailed() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, fileMetadataRepository.markFailedIfUploading(fileId, LocalDateTime.now(clock)));

    }

    @Test
    void markFailedIfUploading_whenUploadingCompleted_updateRowIsZero() {
        FileMetadata fileMetadata = getValidCompletedFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        String checksum = "fake-checksum";
        entityManager.flush();
        entityManager.clear();
        assertEquals(0, fileMetadataRepository.markCompletedIfUploading(fileId, checksum, LocalDateTime.now(clock)));
    }

    @Test
    void markCompletedIfUploading_whenFileIsUploading_changeStateToCompleted() {
        FileMetadata fileMetadata = getValidUploadingFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        String checksum = "fake-checksum";
        assertEquals(1, fileMetadataRepository.markCompletedIfUploading(fileId, checksum, LocalDateTime.now(clock)));
    }

    @Test
    void markCompletedIfUploading_whenUploadingCompleted_updatedRowIsZero() {
        FileMetadata fileMetadata = getValidCompletedFileMetadata();
        FileMetadata persistedUploadingFileMetadata = fileMetadataRepository.saveAndFlush(fileMetadata);
        Long fileId = persistedUploadingFileMetadata.getId();
        entityManager.flush();
        entityManager.clear();

        String checksum = "fake-checksum";
        assertEquals(0, fileMetadataRepository.markCompletedIfUploading(fileId, checksum, LocalDateTime.now(clock)));
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


    @Test
    void findTimedOutFileIds_when10Of100FilesAreDeletingAndTimedOut_thenReturn10Ids() {
        List<Long> expectedTimedOutFileIds = new ArrayList<>();
        LocalDateTime cutoffTime = LocalDateTime.now(clock);

        // 10 file DELETING và đã timeout
        for (int i = 0; i < 10; i++) {
            FileMetadata file = getValidDeletingFileMetadata();
            file.setDeletingAt(cutoffTime.minusMinutes(i + 1L));

            FileMetadata savedFile = fileMetadataRepository.save(file);
            expectedTimedOutFileIds.add(savedFile.getId());
        }

        // 40 file DELETING nhưng chưa timeout
        for (int i = 0; i < 40; i++) {
            FileMetadata file = getValidDeletingFileMetadata();
            file.setDeletingAt(cutoffTime.plusMinutes(i + 1L));

            fileMetadataRepository.save(file);
        }

        // 50 file ở trạng thái COMPLETED
        for (int i = 0; i < 50; i++) {
            FileMetadata file = getValidCompletedFileMetadata();

            // Cố tình đặt deletingAt cũ để chứng minh status vẫn được filter
            file.setDeletingAt(cutoffTime.minusHours(1));

            fileMetadataRepository.save(file);
        }

        fileMetadataRepository.flush();

        // when
        List<Long> actualFileIds = fileMetadataRepository.findTimedOutFiledIds(
                cutoffTime,
                FileState.DELETING,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(actualFileIds)
                .hasSize(10)
                .containsExactlyInAnyOrderElementsOf(expectedTimedOutFileIds);
    }


}