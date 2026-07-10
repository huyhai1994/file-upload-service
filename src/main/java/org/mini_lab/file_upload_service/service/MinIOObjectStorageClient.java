package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinIOObjectStorageClient implements ObjectStorageClient {
    private final MinioClient minioClient;
    private final MinioConfigProperties minioConfigProperties;

    @Override
    public UploadObjectResult upload(FileUploadCommand command) {
        String contentType = command.contentType();
        String originalFileName = command.originalFileName();
        String bucket = minioConfigProperties.bucketName();

        try (InputStream inputStream = command.file().getInputStream()) {

            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(originalFileName)
                            .stream(inputStream, command.file().getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            String etag = response.etag();
            String objectKey = response.object();
            String checksum = response.checksumSHA256();

            return UploadObjectResult.builder()
                    .etag(etag)
                    .objectKey(objectKey)
                    .checksum(checksum)
                    .build();
        } catch (Exception e) {
            throw new ObjectStorageException("Upload failed", e);
        }
    }
}
