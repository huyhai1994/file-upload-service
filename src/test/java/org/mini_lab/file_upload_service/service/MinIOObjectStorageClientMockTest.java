package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinIOObjectStorageClientMockTest {

    @InjectMocks
    MinIOObjectStorageClient minIOObjectStorageClient;

    @Mock
    MinioClient minioClient;


    @Test
    void whenUploadSuccess_thenReturnUploadObjectResult() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        FileUploadCommand command = buildFileUploadCommand();

        ObjectWriteResponse response = mock(ObjectWriteResponse.class);
        when(response.etag()).thenReturn("fake-etag");
        when(response.object()).thenReturn("test.txt");
        when(response.checksumSHA256()).thenReturn("fake-checksum");

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(response);

        UploadObjectResult result = minIOObjectStorageClient.upload(command);

        assertThat(result.etag()).isEqualTo("fake-etag");
        assertThat(result.objectKey()).isEqualTo("test.txt");
        assertThat(result.checksum()).isEqualTo("fake-checksum");

        verify(minioClient).putObject(any(PutObjectArgs.class));

    }

    @Test
    void whenUploadFailed_thenThrowException() throws Exception {
        FileUploadCommand command = buildFileUploadCommand();

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new IOException("MinIO failed"));

        assertThrows(
                ObjectStorageException.class,
                () -> minIOObjectStorageClient.upload(command)
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
                .bucket("test-bucket")
                .contentType("text/plain")
                .originalFileName("test.txt")
                .build();
    }

}