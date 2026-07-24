package org.mini_lab.file_upload_service.service.s3;

import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;

import java.io.InputStream;

public interface ObjectStorageClient {
    UploadObjectResult upload(String objectKey, FileUploadCommand command);

    void delete(String objectKey);

    InputStream getObject(String objectKey);

    boolean exists(String objectKey);
}
