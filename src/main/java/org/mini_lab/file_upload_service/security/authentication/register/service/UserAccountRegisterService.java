package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.register.components.UserRegistrationFactory;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterResponse;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.register.exception.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountRegisterService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRegistrationFactory userRegistrationFactory;

    public RegisterResponse register(RegisterRequest request) {
        User user = userRegistrationFactory.createUser(request);
        final User persistedUser = persistUser(user);
        publishRegisteredEvent(persistedUser);
        return createRegisterResponse(persistedUser);
    }

    private RegisterResponse createRegisterResponse(User persistedUser) {
        return new RegisterResponse(
                persistedUser.getId(),
                persistedUser.getUsername()
        );
    }

    private void publishRegisteredEvent(User savedUser) {
        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        UUID.randomUUID(),
                        NotificationType.WELCOME_EMAIL,
                        savedUser.getUsername(),
                        savedUser.getUsername()
                )
        );
    }

    private User persistUser(User user) {
        final User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UsernameAlreadyExistsException(user.getUsername());
        }
        return savedUser;
    }
}
