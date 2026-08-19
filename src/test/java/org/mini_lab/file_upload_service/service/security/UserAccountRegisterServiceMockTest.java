package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mini_lab.file_upload_service.security.authentication.register.components.UserRegistrationFactory;
import org.mini_lab.file_upload_service.security.authentication.register.dto.RegisterRequest;
import org.mini_lab.file_upload_service.security.authentication.shared.entity.User;
import org.mini_lab.file_upload_service.security.authentication.register.exception.UsernameAlreadyExistsException;
import org.mini_lab.file_upload_service.security.authentication.register.service.UserAccountRegisterService;
import org.mini_lab.file_upload_service.security.authentication.shared.repository.UserRepository;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationType;
import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mini_lab.file_upload_service.support.MockPasswordBuilder.VALID_PASSWORD;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.DEFAULT_USERNAME;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.VALID_EMAIL;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountRegisterServiceMockTest {

    @InjectMocks
    private UserAccountRegisterService userAccountRegisterService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private UserRegistrationFactory userRegistrationFactory;


    @Test
    void register_whenDatabaseUniqueConstraintIsViolated_thenThrowUsernameAlreadyExistsExceptionAndNotPublishEvent() {
        RegisterRequest request = validRegisterRequest();

        when(userRegistrationFactory.createUser(eq(request)))
                .thenReturn(registeredUser(request));

        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate username"
                ));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userAccountRegisterService.register(request)
        );

        verify(userRepository).saveAndFlush(any(User.class));
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void register_whenUserSavedSuccess_thenPublishEvent() {
        RegisterRequest request = validRegisterRequest();

        when(userRegistrationFactory.createUser(eq(request)))
                .thenReturn(registeredUser(request));
        when(userRepository.saveAndFlush(any(User.class)))
                .thenReturn(new User(request.username(), request.password(), request.emailAddress()));

        userAccountRegisterService.register(request);

        verify(userRepository).saveAndFlush(any(User.class));
        ArgumentCaptor<UserRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(UserRegisteredEvent.class);

        verify(applicationEventPublisher)
                .publishEvent(eventCaptor.capture());

        UserRegisteredEvent event = eventCaptor.getValue();

        assertThat(request.username()).isEqualTo(event.username());
        assertThat(request.emailAddress()).isEqualTo(event.emailAddress());
        assertThat(NotificationType.WELCOME_EMAIL).isEqualTo(event.notificationType());
        assertThat(event.eventId()).isNotNull();
    }

    private User registeredUser(RegisterRequest request) {
        return new User(request.username(), request.password());
    }

    private RegisterRequest validRegisterRequest() {
        return registerRequest(DEFAULT_USERNAME, VALID_PASSWORD, VALID_EMAIL);
    }


    private RegisterRequest registerRequest(String username, String password, String email) {
        return new RegisterRequest(username, password, email);
    }

}