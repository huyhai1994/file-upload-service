package org.mini_lab.file_upload_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.dto.ApiError;
import org.mini_lab.file_upload_service.dto.ApiResponse;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
import org.mini_lab.file_upload_service.exception.file_upload.*;
import org.mini_lab.file_upload_service.exception.security.PasswordLengthExceededException;
import org.mini_lab.file_upload_service.exception.security.PasswordTooShortException;
import org.mini_lab.file_upload_service.exception.security.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.exception.security.UsernameLengthExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
            JpaSystemException.class,
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

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException exception
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.USERNAME_ALREADY_EXISTS
        );
    }

    @ExceptionHandler(UsernameLengthExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameLengthExceeded(
            UsernameLengthExceededException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.USERNAME_LENGTH_EXCEEDED
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR
        );
    }

    @ExceptionHandler(PasswordTooShortException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordTooShort(
            PasswordTooShortException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.PASSWORD_TOO_SHORT
        );
    }

    @ExceptionHandler(PasswordLengthExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordLengthExceeded(
            PasswordLengthExceededException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.PASSWORD_LENGTH_EXCEEDED
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
            UsernameNotFoundException exception
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.USER_NOT_FOUND
        );
    }
}