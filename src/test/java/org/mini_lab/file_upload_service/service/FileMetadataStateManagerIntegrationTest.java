package org.mini_lab.file_upload_service.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.entity.FileMetadata;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.repository.FileMetadataRepository;
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

@SpringBootTest
@ActiveProfiles("test")
class FileMetadataStateManagerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    FileMetadataStateManager fileMetadataStateManager;

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    private long fileId = 0;

    @AfterEach
    void tearDown() {
        fileMetadataRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setUp() {
        FileMetadata fileMetadata = fileMetadataRepository.save(buildValidUploadingFileMetadata());
        fileId = fileMetadata.getId();
    }

    @Test
    void whenTwoThreadMarkFailedAtSameTime_onlyOneThreadShouldSucceed() throws InterruptedException {
        int threadCounts = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCounts);


        CountDownLatch readyLatch = new CountDownLatch(threadCounts);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCounts);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        try {
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
                                            fileMetadataStateManager.markFailed(fileId);
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
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void whenTwoThreadsMarkCompletedAtSameTime_onlyOneThreadShouldSucceed()
            throws InterruptedException {

        int threadCount = 2;

        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    readyLatch.countDown();

                    try {
                        startLatch.await();

                        transactionTemplate.executeWithoutResult(status ->
                                fileMetadataStateManager.markCompleted(
                                        fileId,
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

            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow();

            assertEquals(FileState.COMPLETED, metadata.getStatus());
            assertNotNull(metadata.getChecksum());

        } finally {
            executorService.shutdownNow();
        }
    }

    private @NotNull FileMetadata buildValidUploadingFileMetadata() {
        FileMetadata fileMetadata = new FileMetadata();

        fileMetadata.setTitle("Avatar of John Doe");
        fileMetadata.setFileName("avatar.png");
        fileMetadata.setContentType("image/png");
        fileMetadata.setExtension("png");
        fileMetadata.setObjectKey("2026/07/08/8b7c3d0d-f0d1-4a7c-a3a4-7d4ef2e81a55.png");
        fileMetadata.setSize(512_384L);
        fileMetadata.setChecksum("9d5ed678fe57bcca610140957afab571f6d9f1f5e53e7d8d0b8f359bd2d96d8e");
        fileMetadata.setStatus(FileState.UPLOADING);
        return fileMetadata;
    }

}