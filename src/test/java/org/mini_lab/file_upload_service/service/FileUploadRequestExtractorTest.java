package org.mini_lab.file_upload_service.service;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.dto.UploadRequestObjectDTO;
import org.mini_lab.file_upload_service.support.MockObjectBuilder;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadRequestExtractorTest {
    private final FileUploadRequestExtractor fileUploadRequestExtractor = new FileUploadRequestExtractor();

    @Test
    void shouldBeExtractToFileUploadCommand() {
        MultipartFile file = MockObjectBuilder.getTextContentTypeMultipartFile();
        UploadRequestObjectDTO uploadRequestObjectDTO = new UploadRequestObjectDTO(file);
        FileUploadCommand uploadCommand = fileUploadRequestExtractor.extract(uploadRequestObjectDTO);
        assertEquals("test.txt", uploadCommand.originalFileName());
        assertEquals("text/plain", uploadCommand.contentType());
        assertEquals(file.getSize(),uploadCommand.size());
    }
}