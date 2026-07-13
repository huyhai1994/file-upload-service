package org.mini_lab.file_upload_service.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
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
    public UploadObjectResult upload(FileUploadCommand command) {
        MessageDigest messageDigest = messageDigestFactory.createMessageDigest();
        String contentType = command.contentType();
        String originalFileName = command.originalFileName();
        String bucket = minioConfigProperties.bucketName();

        try (InputStream inputStream = command.file().getInputStream();
             DigestInputStream digestInput = new DigestInputStream(inputStream, messageDigest)
        ) {

            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(originalFileName)
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
            throw new ObjectStorageException("Upload failed", e);
        }
    }

    @Override
    public void delete(FileUploadCommand command) {

    }
}
