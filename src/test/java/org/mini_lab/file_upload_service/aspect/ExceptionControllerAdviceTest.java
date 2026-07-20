package org.mini_lab.file_upload_service.aspect;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.ErrorDetail;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionControllerAdviceTest {
    private final ExceptionControllerAdvice handler = new ExceptionControllerAdvice();

    @Test
    void handleEmptyFileException_shouldReturn400StatusCode() {
        ResponseEntity<ErrorDetail> response = handler.handleBadRequest(new EmptyFileException());
        assertBadRequest(response, "File Empty");
    }

    @Test
    void handleInvalidFileNameException_shouldReturn400StatusCode() {
        ResponseEntity<ErrorDetail> response = handler.handleBadRequest(new InvalidFilenameException());
        assertBadRequest(response, "File name not valid");
    }

    @Test
    void handleInvalidFileExtensionException_shouldReturn400StatusCode() {
        ResponseEntity<ErrorDetail> response = handler.handleBadRequest(new InvalidFileExtensionException());
        assertBadRequest(response, "File extension not valid");
    }

    @Test
    void handleInvalidMimeTypeException_shouldReturn400StatusCode() {
        ResponseEntity<ErrorDetail> response = handler.handleBadRequest(new InvalidMimeTypeException());
        assertBadRequest(response, "Mime type  not valid");
    }


    @Test
    void handleInternalServerException_shouldReturn500StatusCode() {
        ResponseEntity<ErrorDetail> response = handler.handleInternalServerError(new InternalServerException());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Error", Objects.requireNonNull(response.getBody()).getMessage());


    }

    @Test
    void handleFileNotAvailable_shouldReturn409StatusCode() {
        Long fileId = 1L;
        FileState state = FileState.UPLOADING;
        ResponseEntity<ErrorDetail> response = handler.handleFileNotAvailable(new FileNotAvailableException(fileId, state));
        assertEquals(HttpStatus.valueOf(409), response.getStatusCode());
        assertEquals(String.format("File with %d id and state %s not available", fileId, state), Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleFileNotFound_shouldReturn404StatusCode() {
        ResponseEntity<ErrorDetail> response = handler.handleFileNotFound(new FileNotFoundException());
        assertEquals(HttpStatus.valueOf(404), response.getStatusCode());
        assertEquals("File not found!", Objects.requireNonNull(response.getBody()).getMessage());
    }


    private void assertBadRequest(
            ResponseEntity<ErrorDetail> response,
            String expectedMessage
    ) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }
}