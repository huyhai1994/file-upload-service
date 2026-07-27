package org.mini_lab.file_upload_service.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.exception.file_upload.InvalidFilenameException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockObjectBuilder.*;

class FileNameValidatorTest {

    private final FileNameValidator fileNameValidator = new FileNameValidator();

    @Test
    void whenFileNameIsEmpty_shouldThrowInvalidFilenameException() {
        assertThrows(InvalidFilenameException.class,
                () -> fileNameValidator.validate(getFileUploadCommand(getEmptyFilenameMultipartFile())));
    }

    @Test
    void whenFileNameNotEmpty_doesNotThrowException() {
        assertDoesNotThrow(
                () -> fileNameValidator.validate(getFileUploadCommand(getTextContentTypeMultipartFile())));
    }

}