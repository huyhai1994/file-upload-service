package org.mini_lab.file_upload_service.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class MinIOObjectStorageClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MinIOObjectStorageClient minIOObjectStorageClient;

    private static @NotNull MockMultipartFile getMockMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello MinIO".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void uploadSuccess_thenReturnUploadResult() {
        MockMultipartFile file = getMockMultipartFile();
        UploadObjectResult result =
                minIOObjectStorageClient
                        .upload(FileUploadCommand.builder()
                                .originalFileName(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .size(file.getSize())
                                .file(file)
                                .build());
        assertNotNull(result);
    }

    @Test
    void upload10Files_withLimitedConcurrency_notThrowException() throws Exception {
        int numberOfTasks = 10;
        int poolSize = 50;

        CountDownLatch readyLatch = new CountDownLatch(numberOfTasks);
        CountDownLatch startLatch = new CountDownLatch(1);
        MockMultipartFile file = getMockMultipartFile();

        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

        List<Future<UploadObjectResult>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {

            Future<UploadObjectResult> future = executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                return minIOObjectStorageClient.upload(
                        FileUploadCommand.builder()
                                .originalFileName(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .size(file.getSize())
                                .file(file)
                                .build()
                );
            });

            futures.add(future);
        }

        assertTrue(readyLatch.await(60, TimeUnit.SECONDS));

        startLatch.countDown();

        List<UploadObjectResult> results = new ArrayList<>();

        for (Future<UploadObjectResult> future : futures) {
            UploadObjectResult result = future.get(30, TimeUnit.SECONDS);

            assertNotNull(result);
            assertNotNull(result.etag());

            results.add(result);
        }

        assertEquals(numberOfTasks, results.size());

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }
}

