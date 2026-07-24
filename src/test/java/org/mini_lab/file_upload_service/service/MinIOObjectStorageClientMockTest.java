package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.component.MessageDigestFactory;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.file_upload.UploadObjectResult;
import org.mini_lab.file_upload_service.exception.file_upload.ObjectStorageException;
import org.mini_lab.file_upload_service.service.s3.MinIOObjectStorageClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getFileUploadCommand;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.getTextContentTypeMultipartFile;
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
        FileUploadCommand command = getFileUploadCommand(getTextContentTypeMultipartFile());

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
        FileUploadCommand command = getFileUploadCommand(getTextContentTypeMultipartFile());

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


}