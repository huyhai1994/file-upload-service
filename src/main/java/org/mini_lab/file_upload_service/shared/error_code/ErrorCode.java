package org.mini_lab.file_upload_service.shared.error_code;

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
    USERNAME_COULD_NOT_BE_NULL("Username couldn't be null"),
    USER_NOT_FOUND("User couldn't found"),
    INVALID_CREDENTIALS("Invalid username or password"),

    PASSWORD_TOO_SHORT("Password must contain at least 8 characters"),
    PASSWORD_LENGTH_EXCEEDED("Password must not exceed 72 characters"),
    PASSWORD_COULD_NOT_BE_NULL("Password couldn't be null"),

    // Request
    VALIDATION_ERROR("Request validation failed"),

    INVALID_REQUEST_BODY("Invalid request body"),

    BAD_CREDENTIAL("Invalid username or password!"),

    // Common
    INTERNAL_SERVER_ERROR("Internal server error"),

    TOO_MANY_REQUESTS("Too Many Requests"),

    // Rate limiter
    Rate_LIMITER_UNAVAILABLE("Redis rate limiter is unavailable"),

    // Login
    USER_ACCOUNT_LOCKED("User account is temporarily locked due to multiple failed login attempts");

    private final String defaultMessage;
}