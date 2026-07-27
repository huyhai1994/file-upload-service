package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.file_upload.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
import org.mini_lab.file_upload_service.service.state_manager.FileMetadataStateManager;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;

@SpringBootTest
@ActiveProfiles("test")
class FileMetadataStateManagerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    FileMetadataStateManager fileMetadataStateManager;

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    private long uploadingFileMetadataId = 0;

    private long completedFileMetadataId = 0;

    private long deletingFileMetadataId = 0;

    private final int threadCounts = 2;

    private final ExecutorService executorService = Executors.newFixedThreadPool(threadCounts);

    private CountDownLatch readyLatch;
    private CountDownLatch startLatch;
    private CountDownLatch doneLatch;
    private AtomicInteger successCount;
    private AtomicInteger failureCount;

    @AfterEach
    void tearDown() {
        fileMetadataRepository.deleteAllInBatch();
        executorService.shutdownNow();
    }

    @BeforeEach
    void setUp() {
        readyLatch = new CountDownLatch(threadCounts);
        startLatch = new CountDownLatch(1);
        doneLatch = new CountDownLatch(threadCounts);
        successCount = new AtomicInteger();
        failureCount = new AtomicInteger();
        uploadingFileMetadataId = fileMetadataRepository.save(getValidUploadingFileMetadata()).getId();
        completedFileMetadataId = fileMetadataRepository.save(getValidCompletedFileMetadata()).getId();
        deletingFileMetadataId = fileMetadataRepository.save(getValidDeletingFileMetadata()).getId();
    }

    @Test
    void whenTwoThreadMarkFailedAtSameTime_onlyOneThreadShouldSucceed() throws InterruptedException {
        for (int i = 0; i < threadCounts; i++) {
            executorService.submit(() -> {
                        readyLatch.countDown();
                        try {
                            startLatch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        transactionTemplate.executeWithoutResult(
                                status -> {

                                    try {
                                        fileMetadataStateManager.markFailed(uploadingFileMetadataId);
                                        successCount.incrementAndGet();
                                    } catch (InvalidStateTransitionException e) {
                                        failureCount.incrementAndGet();
                                    } finally {
                                        doneLatch.countDown();
                                    }
                                }
                        );
                    }
            );
        }
        assertTrue(
                readyLatch.await(10, TimeUnit.SECONDS),
                "Threads were not ready in time"
        );

        startLatch.countDown();

        assertTrue(
                doneLatch.await(10, TimeUnit.SECONDS),
                "Threads did not finish in time"
        );

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());
    }

    @Test
    void whenTwoThreadsMarkCompletedAtSameTime_onlyOneThreadShouldSucceed()
            throws InterruptedException {
        for (int i = 0; i < threadCounts; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();

                try {
                    startLatch.await();

                    transactionTemplate.executeWithoutResult(status ->
                            fileMetadataStateManager.markCompleted(
                                    uploadingFileMetadataId,
                                    UUID.randomUUID().toString()
                            )
                    );

                    successCount.incrementAndGet();

                } catch (Exception exception) {
                    failureCount.incrementAndGet();

                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertTrue(
                readyLatch.await(10, TimeUnit.SECONDS),
                "Threads were not ready in time"
        );

        startLatch.countDown();

        assertTrue(
                doneLatch.await(10, TimeUnit.SECONDS),
                "Threads did not finish in time"
        );

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        FileMetadata metadata = fileMetadataRepository.findById(uploadingFileMetadataId)
                .orElseThrow();

        assertEquals(FileState.COMPLETED, metadata.getStatus());
        assertNotNull(metadata.getChecksum());
    }

    @Test
    void whenTwoThreadsMarkDeletingAtSameTime_onlyOneThreadShouldSucceed()
            throws InterruptedException {

        for (int i = 0; i < threadCounts; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();

                try {
                    startLatch.await();

                    transactionTemplate.executeWithoutResult(status ->
                            fileMetadataStateManager.markDeleting(
                                    completedFileMetadataId
                            )
                    );

                    successCount.incrementAndGet();

                } catch (Exception exception) {
                    failureCount.incrementAndGet();

                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertTrue(
                readyLatch.await(10, TimeUnit.SECONDS),
                "Threads were not ready in time"
        );

        startLatch.countDown();

        assertTrue(
                doneLatch.await(10, TimeUnit.SECONDS),
                "Threads did not finish in time"
        );

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        FileMetadata metadata = fileMetadataRepository.findById(completedFileMetadataId)
                .orElseThrow();

        assertEquals(FileState.DELETING, metadata.getStatus());
        assertNotNull(metadata.getChecksum());

    }

    @Test
    void whenTwoThreadsMarkDeletedAtSameTime_onlyOneThreadShouldSucceed()
            throws InterruptedException {

        for (int i = 0; i < threadCounts; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();

                try {
                    startLatch.await();

                    transactionTemplate.executeWithoutResult(status ->
                            fileMetadataStateManager.markDeleted(
                                    deletingFileMetadataId
                            )
                    );

                    successCount.incrementAndGet();

                } catch (Exception exception) {
                    failureCount.incrementAndGet();

                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertTrue(
                readyLatch.await(10, TimeUnit.SECONDS),
                "Threads were not ready in time"
        );

        startLatch.countDown();

        assertTrue(
                doneLatch.await(10, TimeUnit.SECONDS),
                "Threads did not finish in time"
        );

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        FileMetadata metadata = fileMetadataRepository.findById(deletingFileMetadataId)
                .orElseThrow();

        assertEquals(FileState.DELETED, metadata.getStatus());
        assertNotNull(metadata.getChecksum());

    }
}