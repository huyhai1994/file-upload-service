package org.mini_lab.file_upload_service.service.security;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.enums.file_upload.ErrorCode;
import org.mini_lab.file_upload_service.exception.security.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.exception.security.UsernameLengthExceededException;
import org.mini_lab.file_upload_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UsernameVerifyService {

    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MAX_USERNAME_LENGTH = 50;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-z0-9_]+$");

    private final UserRepository userRepository;

    public void verify(String username) {
        validateNull(username);
        validateLeadingOrTrailingSpaces(username);
        validateLength(username);
        validateAllowedCharacters(username);
        validateNotExists(username);
    }

    private void validateNull(String username) {
        if (username == null) {
            throw new InputMismatchException(
                    ErrorCode.USERNAME_COULD_NOT_BE_NULL.getDefaultMessage()
            );
        }
    }

    private void validateLeadingOrTrailingSpaces(String username) {
        if (!username.equals(username.trim())) {
            throw new InputMismatchException(
                    "Username must not contain leading or trailing spaces"
            );
        }
    }

    private void validateLength(String username) {
        int length = username.length();

        if (length < MIN_USERNAME_LENGTH || length > MAX_USERNAME_LENGTH) {
            throw new UsernameLengthExceededException(
                    MIN_USERNAME_LENGTH,
                    MAX_USERNAME_LENGTH
            );
        }
    }

    private void validateAllowedCharacters(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new InputMismatchException(
                    "Username may contain only lowercase letters, digits, and underscore"
            );
        }
    }

    private void validateNotExists(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
    }
}