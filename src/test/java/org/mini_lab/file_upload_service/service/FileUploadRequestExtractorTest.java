package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.file_upload.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.file_upload.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.service.upload.FileUploadRequestExtractor;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadRequestExtractorTest {
    private final FileUploadRequestExtractor fileUploadRequestExtractor = new FileUploadRequestExtractor();

    @Test
    void shouldBeExtractToFileUploadCommand() {
        MultipartFile file = MockObjectBuilder.getTextContentTypeMultipartFile();
        String title = "test";
        UploadRequestObjectDTO uploadRequestObjectDTO = new UploadRequestObjectDTO(file, title);
        FileUploadCommand uploadCommand = fileUploadRequestExtractor.extract(uploadRequestObjectDTO);
        assertEquals("test.txt", uploadCommand.originalFileName());
        assertEquals("text/plain", uploadCommand.contentType());
        assertEquals(file.getSize(), uploadCommand.size());
    }
}