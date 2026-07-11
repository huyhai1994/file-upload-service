package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.exception.EmptyFileException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmptyFileValidatorTest {

    private final EmptyFileValidator emptyFileValidator =
            new EmptyFileValidator();

    @Test
    void whenFileIsEmpty_thenThrowEmptyFileException() {
        FileUploadCommand command =
                buildFileUploadCommand(createEmptyMultipartFile());

        assertThrows(
                EmptyFileException.class,
                () -> emptyFileValidator.validate(command)
        );
    }

    @Test
    void whenFileIsNull_thenThrowEmptyFileException() {
        FileUploadCommand command = FileUploadCommand.builder()
                .file(null)
                .build();

        assertThrows(
                EmptyFileException.class,
                () -> emptyFileValidator.validate(command)
        );
    }

    @Test
    void whenFileIsNotEmpty_thenValidationDoesNotThrow() {
        FileUploadCommand command =
                buildFileUploadCommand(createNonEmptyMultipartFile());

        assertDoesNotThrow(
                () -> emptyFileValidator.validate(command)
        );
    }

    @Test
    void orderShouldBe10() {
        assertEquals(10, emptyFileValidator.order());
    }

    private static MockMultipartFile createEmptyMultipartFile() {
        return new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );
    }

    private static MockMultipartFile createNonEmptyMultipartFile() {
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