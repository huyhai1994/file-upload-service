package org.mini_lab.file_upload_service.security.jwt.components;

import org.mini_lab.file_upload_service.security.jwt.error_code.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class JwtAccessTokenValidator {

    public void validateUserDetails(UserDetails userDetails) {
        Objects.requireNonNull(
                userDetails,
                ErrorCode.USER_DETAILS_REQUIRED.getDefaultMessage()
        );
    }

    public void validateAccessToken(String accessToken) {
        Objects.requireNonNull(
                accessToken,
                ErrorCode.ACCESS_TOKEN_REQUIRED.getDefaultMessage()
        );
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.JWT_SUBJECT_REQUIRED.getDefaultMessage());
        }
    }

    public List<?> validateRawAuthorities(Object authoritiesClaim) {
        if (!(authoritiesClaim instanceof List<?> rawAuthorities)) {
            throw new IllegalArgumentException(
                    ErrorCode.AUTHORIZE_CLAIMS_IS_ARRAY.getDefaultMessage()
            );
        }

        return rawAuthorities;
    }

    public String validateAuthority(Object authority) {
        if (!(authority instanceof String value)) {
            throw new IllegalArgumentException(
                    ErrorCode.AUTHORITY_IS_STRING.getDefaultMessage()
            );
        }

        return value;
    }

}
