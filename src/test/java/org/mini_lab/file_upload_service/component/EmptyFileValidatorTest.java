package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.file_upload.component.EmptyFileValidator;
import org.mini_lab.file_upload_service.file_upload.dto.FileUploadCommand;
import org.mini_lab.file_upload_service.file_upload.exception.EmptyFileException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;

class EmptyFileValidatorTest {

    private final EmptyFileValidator emptyFileValidator =
            new EmptyFileValidator();

    @Test
    void whenFileIsEmpty_thenThrowEmptyFileException() {
        FileUploadCommand command =
                getFileUploadCommand(getEmptyMultipartFile());

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
                getFileUploadCommand(getTextContentTypeMultipartFile());

        assertDoesNotThrow(
                () -> emptyFileValidator.validate(command)
        );
    }

    @Test
    void orderShouldBe10() {
        assertEquals(10, emptyFileValidator.order());
    }
}