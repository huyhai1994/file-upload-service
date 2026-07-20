package org.mini_lab.file_upload_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.dto.ErrorDetail;
import org.mini_lab.file_upload_service.exception.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler({
            EmptyFileException.class,
            InvalidFilenameException.class,
            InvalidFileExtensionException.class,
            InvalidMimeTypeException.class
    })
    public ResponseEntity<ErrorDetail> handleBadRequest(Exception ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler({
            FileNotAvailableException.class
    })
    public ResponseEntity<ErrorDetail> handleFileNotAvailable(Exception ex) {

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatusCode.valueOf(409))
                .body(errorDetail);

    }

    @ExceptionHandler({
            FileNotFoundException.class
    })
    public ResponseEntity<ErrorDetail> handleFileNotFound(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());

        return ResponseEntity
                .status(HttpStatusCode.valueOf(404))
                .body(errorDetail);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDetail> handleInternalServerError(Exception ex) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(ex.getMessage());

        return ResponseEntity
                .internalServerError()
                .body(errorDetail);
    }

    private ResponseEntity<ErrorDetail> badRequest(String message) {
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setMessage(message);

        return ResponseEntity
                .badRequest()
                .body(errorDetail);
    }
}
