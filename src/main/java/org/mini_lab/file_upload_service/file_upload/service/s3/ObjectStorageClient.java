package org.mini_lab.file_upload_service.file_upload.service.s3;

import org.mini_lab.file_upload_service.file_upload.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.dto.file_upload.UploadObjectResult;

import java.io.InputStream;

public interface ObjectStorageClient {
    UploadObjectResult upload(String objectKey, FileUploadCommand command);

    void delete(String objectKey);

    InputStream getObject(String objectKey);

    boolean exists(String objectKey);
}
