package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.mini_lab.file_upload_service.service.s3.MinIOObjectStorageClient;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.TrafficBlockedSimulationTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getFileUploadCommand;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;


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
        FileUploadCommand command = getFileUploadCommand(getTextContentTypeMultipartFile());
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
        FileUploadCommand fileUploadCommand = getFileUploadCommand(getTextContentTypeMultipartFile());
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
    void uploadTwoFilesConcurrency_notThrowException() throws Exception {
        int numberOfTasks = 2;
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
                        getFileUploadCommand(getTextContentTypeMultipartFile()));
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

    @Test
    void whenTrafficIsBlocked_thenUploadShouldFail() throws IOException {

        try (TrafficBlockedSimulationTools ignored = TrafficBlockedSimulationTools.applyTo(minioProxy)) {
            assertThrows(
                    ObjectStorageException.class,
                    () -> minIOObjectStorageClient.upload(
                            "files/test.txt",
                            getFileUploadCommand(
                                    getTextContentTypeMultipartFile()
                            )
                    )
            );
        }
    }

    @Test
    void whenGetObject_thenDownloadBytesAndUploadBytesShouldBeSame() throws IOException {
        String objectKey = "test-objectKey";
        byte[] uploadBytes = "this is the complete file content".getBytes(StandardCharsets.UTF_8);
        try {
            minioClient.putObject(PutObjectArgs.builder()

                    .bucket(minioConfigProperties.bucketName())
                    .object(objectKey)
                    .stream(
                            new ByteArrayInputStream(uploadBytes)
                            , uploadBytes.length, -1)
                    .contentType("text/plain")
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] downloadBytes;
        try (InputStream inputStream = minIOObjectStorageClient.getObject(objectKey)) {
            downloadBytes = inputStream.readAllBytes();
        }
        assertArrayEquals(uploadBytes, downloadBytes);
    }

    @Test
    void whenGetObject_ifObjectNotExistThrowObjectStorageException() {
        String objectKey = "test-objectKey";
        assertThrows(ObjectStorageException.class, () -> minIOObjectStorageClient.getObject(objectKey));
    }
}

