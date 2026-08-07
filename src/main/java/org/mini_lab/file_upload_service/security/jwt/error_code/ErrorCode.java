package org.mini_lab.file_upload_service.security.jwt.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    USER_DETAILS_REQUIRED("userDetails must not be null"),
    ACCESS_TOKEN_REQUIRED("accessToken must not be null"),
    JWT_SUBJECT_REQUIRED("JWT subject must not be null or blank"),
    TOKEN_REQUIRED("token must not be null"),
    AUTHORIZE_CLAIMS_IS_ARRAY("JWT authorities claim must be an array"),
    AUTHORITY_IS_STRING("JWT authority must be a string");
    private final String defaultMessage;
}

