package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getFileUploadCommand;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getMockTextContentTypeMultipartFile;


@SpringBootTest
@ActiveProfiles("test")
class MinIOObjectStorageClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MinIOObjectStorageClient minIOObjectStorageClient;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioConfigProperties minioConfigProperties;

    @Test
    void uploadSuccess_thenReturnUploadResult() {
        FileUploadCommand command = getFileUploadCommand(getMockTextContentTypeMultipartFile());
        String objectKey = UUID.randomUUID().toString();
        UploadObjectResult result =
                minIOObjectStorageClient
                        .upload(objectKey, command);
        assertNotNull(result);
        StatObjectResponse stat;
        try {
            stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfigProperties.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }

        assertEquals(objectKey, stat.object());
        assertEquals(command.size(), stat.size());
    }

    @Test
    void deleteSuccess_thenDoesNotThrowException() {
        FileUploadCommand fileUploadCommand = getFileUploadCommand(getMockTextContentTypeMultipartFile());
        String objectKey = UUID.randomUUID().toString();
        minIOObjectStorageClient.upload(objectKey, fileUploadCommand);

        assertDoesNotThrow(() -> minIOObjectStorageClient.delete(objectKey));
        assertThrows(
                ErrorResponseException.class,
                () -> minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(minioConfigProperties.bucketName())
                                .object(objectKey)
                                .build()
                )
        );
    }

    @Test
    void upload10Files_withLimitedConcurrency_notThrowException() throws Exception {
        int numberOfTasks = 10;
        int poolSize = 50;

        CountDownLatch readyLatch = new CountDownLatch(numberOfTasks);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

        List<Future<UploadObjectResult>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {

            Future<UploadObjectResult> future = executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                return minIOObjectStorageClient.upload(UUID.randomUUID().toString(),
                        getFileUploadCommand(getMockTextContentTypeMultipartFile()));
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

