package org.mini_lab.file_upload_service.service.security;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.exception.security.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.exception.security.UsernameLengthExceededException;
import org.mini_lab.file_upload_service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsernameVerifyService {

    private static final int MAX_USERNAME_LENGTH = 100;

    private final UserRepository userRepository;

    public void verify(String username) {
        validateLength(username);
        validateNotExists(username);
    }

    private void validateLength(String username) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new UsernameLengthExceededException(MAX_USERNAME_LENGTH);
        }
    }

    private void validateNotExists(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
    }
}