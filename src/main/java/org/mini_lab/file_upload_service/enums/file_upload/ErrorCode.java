package org.mini_lab.file_upload_service.enums.file_upload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    EMPTY_FILE("File empty"),
    INVALID_FILE_NAME("File name not valid"),
    INVALID_FILE_EXTENSION("File extension not valid"),
    INVALID_MIME_TYPE("Mime type not valid"),

    FILE_NOT_FOUND("File not found"),
    FILE_NOT_AVAILABLE("File not available"),

    INTERNAL_SERVER_ERROR("Internal server error"),
    CANNOT_READ_METADATA("Cannot read file metadata");


    private final String defaultMessage;
}
