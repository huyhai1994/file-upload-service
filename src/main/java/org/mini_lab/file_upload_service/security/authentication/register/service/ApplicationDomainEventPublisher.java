package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishEvent(UserRegisteredEvent event) {
        eventPublisher.publishEvent(event);
    }
}
