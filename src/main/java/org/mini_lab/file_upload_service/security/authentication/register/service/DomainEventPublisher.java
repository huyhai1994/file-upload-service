package org.mini_lab.file_upload_service.security.authentication.register.service;

import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;

public interface DomainEventPublisher {
    void publishEvent(UserRegisteredEvent event);
}
