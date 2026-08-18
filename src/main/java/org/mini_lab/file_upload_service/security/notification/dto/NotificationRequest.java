package org.mini_lab.file_upload_service.security.notification.dto;

import java.util.UUID;

public record NotificationRequest(
        UUID eventId,
        NotificationType notificationType,
        String emailAddress,
        String username
) {
}
