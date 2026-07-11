package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.InvalidFilenameException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileNameValidatorTest {

    private final FileNameValidator fileNameValidator = new FileNameValidator();

    @Test
    void whenFileNameIsEmpty_shouldThrowInvalidFilenameException() {
        assertThrows(InvalidFilenameException.class,
                () -> fileNameValidator.validate(
                        buildFileUploadCommand(createEmptyFilenameMultipartFile())));
    }

    @Test
    void whenFileNameNotEmpty_doesNotThrowException() {
        assertDoesNotThrow(
                () -> fileNameValidator.validate(
                        buildFileUploadCommand(createNomEmptyFilenameMultipartFile())));
    }

    private static MockMultipartFile createEmptyFilenameMultipartFile() {
        return new MockMultipartFile(
                "file",
                "",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MockMultipartFile createNomEmptyFilenameMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static FileUploadCommand buildFileUploadCommand(
            MultipartFile multipartFile
    ) {
        return FileUploadCommand.builder()
                .file(multipartFile)
                .contentType(multipartFile.getContentType())
                .originalFileName(multipartFile.getOriginalFilename())
                .build();
    }

}