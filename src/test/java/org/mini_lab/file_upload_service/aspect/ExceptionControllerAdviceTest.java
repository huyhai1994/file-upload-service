package org.mini_lab.file_upload_service.aspect;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.dto.ApiError;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.entity.FileState;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
import org.mini_lab.file_upload_service.exception.file_upload.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.CannotCreateTransactionException;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionControllerAdviceTest {

    private final ExceptionControllerAdvice advice =
            new ExceptionControllerAdvice();

    @Test
    void handleEmptyFile_shouldReturnBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleEmptyFile(
                        new EmptyFileException()
                );

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                ErrorCode.EMPTY_FILE
        );
    }

    @Test
    void handleInvalidFilename_shouldReturnBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInvalidFilename(
                        new InvalidFilenameException()
                );

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_FILE_NAME
        );
    }

    @Test
    void handleInvalidFileExtension_shouldReturnBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInvalidFileExtension(
                        new InvalidFileExtensionException()
                );

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_FILE_EXTENSION
        );
    }

    @Test
    void handleInvalidMimeType_shouldReturnBadRequest() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInvalidMimeType(
                        new InvalidMimeTypeException()
                );

        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_MIME_TYPE
        );
    }

    @Test
    void handleFileNotFound_shouldReturnNotFound() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleFileNotFound(
                        new FileNotFoundException()
                );

        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                ErrorCode.FILE_NOT_FOUND
        );
    }

    @Test
    void handleFileNotAvailable_shouldReturnConflict() {
        Long id = 1L;
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleFileNotAvailable(
                        new FileNotAvailableException(id, FileState.COMPLETED)
                );

        assertErrorResponse(
                response,
                HttpStatus.CONFLICT,
                ErrorCode.FILE_NOT_AVAILABLE
        );
    }

    @Test
    void handleInternalServerException_shouldReturnInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInternalServerError(
                        new InternalServerException()
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    @Test
    void handleFileReadException_shouldReturnInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInternalServerError(
                        new FileReadException(new Exception())
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    @Test
    void handleCannotCreateTransactionException_shouldReturnInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInternalServerError(
                        new CannotCreateTransactionException("Can't create transaction")
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    @Test
    void handleJpaSystemException_shouldReturnInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response =
                advice.handleInternalServerError(
                        new JpaSystemException(new RuntimeException())
                );

        assertErrorResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    private void assertErrorResponse(
            ResponseEntity<ApiResponse<Void>> response,
            HttpStatus expectedStatus,
            ErrorCode expectedErrorCode
    ) {
        assertThat(response.getStatusCode())
                .isEqualTo(expectedStatus);

        assertThat(response.getBody())
                .isNotNull();

        ApiResponse<Void> body = response.getBody();

        assertThat(body.success()).isFalse();
        assertThat(body.data()).isNull();
        assertThat(body.error()).isNotNull();

        ApiError error = body.error();

        assertThat(error.code())
                .isEqualTo(expectedErrorCode.name());

        assertThat(error.message())
                .isEqualTo(expectedErrorCode.getDefaultMessage());
    }
}