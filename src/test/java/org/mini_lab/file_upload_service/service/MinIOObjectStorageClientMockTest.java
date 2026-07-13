package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.component.MessageDigestFactory;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinIOObjectStorageClientMockTest {

    @InjectMocks
    MinIOObjectStorageClient minIOObjectStorageClient;

    @Mock
    MessageDigestFactory messageDigestFactory;

    @Mock
    MinioClient minioClient;

    @Mock
    MinioConfigProperties minioConfigProperties;


    @Test
    void whenUploadSuccess_thenReturnUploadObjectResult() throws Exception {
        FileUploadCommand command = buildFileUploadCommand();

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        ObjectWriteResponse response = mock(ObjectWriteResponse.class);

        when(messageDigestFactory.createMessageDigest())
                .thenReturn(messageDigest);
        when(minioConfigProperties.bucketName())
                .thenReturn("file-upload-test");
        when(response.etag())
                .thenReturn("fake-etag");
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(response);

        UploadObjectResult result =
                minIOObjectStorageClient.upload(UUID.randomUUID().toString(), command);

        assertThat(result.etag()).isEqualTo("fake-etag");
        assertThat(result.checksum()).isNotBlank();

        verify(messageDigestFactory).createMessageDigest();
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void whenUploadFailed_thenThrowException() throws Exception {
        FileUploadCommand command = buildFileUploadCommand();

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new IOException("MinIO failed"));
        when(messageDigestFactory.createMessageDigest()).thenReturn(messageDigest);
        when(minioConfigProperties.bucketName()).thenReturn("file-upload-test");

        assertThrows(
                ObjectStorageException.class,
                () -> minIOObjectStorageClient.upload(UUID.randomUUID().toString(), command)
        );

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    private FileUploadCommand buildFileUploadCommand() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello minio".getBytes(StandardCharsets.UTF_8)
        );

        return FileUploadCommand.builder()
                .file(file)
                .contentType(file.getContentType())
                .size(file.getSize())
                .originalFileName(file.getOriginalFilename())
                .build();
    }

}