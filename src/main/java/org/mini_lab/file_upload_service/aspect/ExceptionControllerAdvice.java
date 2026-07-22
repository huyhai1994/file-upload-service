package org.mini_lab.file_upload_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.dto.ApiError;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.enums.ErrorCode;
import org.mini_lab.file_upload_service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyFile(
            EmptyFileException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.EMPTY_FILE
        );
    }

    @ExceptionHandler(InvalidFilenameException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFilename(
            InvalidFilenameException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_FILE_NAME
        );
    }

    @ExceptionHandler(InvalidFileExtensionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFileExtension(
            InvalidFileExtensionException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_FILE_EXTENSION
        );
    }

    @ExceptionHandler(InvalidMimeTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidMimeType(
            InvalidMimeTypeException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_MIME_TYPE
        );
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileNotFound(
            FileNotFoundException exception
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.FILE_NOT_FOUND
        );
    }

    @ExceptionHandler(FileNotAvailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileNotAvailable(
            FileNotAvailableException exception
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.FILE_NOT_AVAILABLE
        );
    }

    @ExceptionHandler({
            FileReadException.class,
            InternalServerException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError(
            Exception exception
    ) {
        log.error(
                "Internal server error: exceptionType={}, message={}",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception
    ) {
        log.error(
                "Unexpected exception: exceptionType={}, message={}",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode
    ) {
        ApiError error = new ApiError(
                errorCode.name(),
                errorCode.getDefaultMessage()
        );

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(error));
    }
}