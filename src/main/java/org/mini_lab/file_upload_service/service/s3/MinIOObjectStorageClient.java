package org.mini_lab.file_upload_service.service.s3;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.component.MessageDigestFactory;
import org.mini_lab.file_upload_service.configuration.MinioConfigProperties;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;
import org.mini_lab.file_upload_service.exception.ObjectStorageException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class MinIOObjectStorageClient implements ObjectStorageClient {
    private final MinioClient minioClient;
    private final MinioConfigProperties minioConfigProperties;
    private final MessageDigestFactory messageDigestFactory;

    @Override
    public UploadObjectResult upload(String objectKey, FileUploadCommand command) {
        MessageDigest messageDigest = messageDigestFactory.createMessageDigest();
        String contentType = command.contentType();
        String bucket = minioConfigProperties.bucketName();

        try (InputStream inputStream = command.file().getInputStream();
             DigestInputStream digestInput = new DigestInputStream(inputStream, messageDigest)
        ) {

            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(digestInput, command.size(), -1)
                            .contentType(contentType)
                            .build()
            );
            String checksum = HexFormat.of().formatHex(messageDigest.digest());
            String eTag = response.etag();

            return UploadObjectResult.builder()
                    .etag(eTag)
                    .checksum(checksum)
                    .build();
        } catch (Exception e) {
            throw new ObjectStorageException("Upload object failed, bucket=%s, object key=%s".formatted(bucket, objectKey), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        String bucket = minioConfigProperties.bucketName();
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfigProperties.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageException("Delete object failed, bucket=%s, object key=%s".formatted(bucket, objectKey), e);
        }
    }

    @Override
    public InputStream getObject(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfigProperties.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageException(String.format("Can't get Object with %s", objectKey), e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfigProperties.bucketName())
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
