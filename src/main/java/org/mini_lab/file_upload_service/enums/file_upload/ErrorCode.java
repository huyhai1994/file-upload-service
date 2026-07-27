package org.mini_lab.file_upload_service.enums.file_upload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // File
    EMPTY_FILE("File empty"),
    INVALID_FILE_NAME("File name not valid"),
    INVALID_FILE_EXTENSION("File extension not valid"),
    INVALID_MIME_TYPE("Mime type not valid"),

    FILE_NOT_FOUND("File not found"),
    FILE_NOT_AVAILABLE("File not available"),
    CANNOT_READ_METADATA("Cannot read file metadata"),

    // Authentication / registration
    USERNAME_ALREADY_EXISTS("Username already exists"),
    USERNAME_LENGTH_EXCEEDED("Username must not exceed 100 characters"),

    PASSWORD_TOO_SHORT("Password must contain at least 8 characters"),
    PASSWORD_LENGTH_EXCEEDED("Password must not exceed 72 characters"),

    // Request
    VALIDATION_ERROR("Request validation failed"),
    INVALID_REQUEST_BODY("Invalid request body"),

    // Common
    INTERNAL_SERVER_ERROR("Internal server error");

    private final String defaultMessage;
}