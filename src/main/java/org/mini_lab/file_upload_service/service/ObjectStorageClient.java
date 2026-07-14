package org.mini_lab.file_upload_service.service;

import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadObjectResult;

public interface ObjectStorageClient {
    UploadObjectResult upload(String objectKey, FileUploadCommand command);

    void delete(String objectKey);
}
