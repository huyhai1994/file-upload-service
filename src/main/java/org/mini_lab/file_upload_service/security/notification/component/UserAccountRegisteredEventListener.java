package org.mini_lab.file_upload_service.security.notification.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;
import org.mini_lab.file_upload_service.security.notification.dto.NotificationType;
import org.mini_lab.file_upload_service.security.notification.service.NotificationServiceClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAccountRegisteredEventListener {

    private final NotificationServiceClient notificationClient;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(UserRegisteredEvent event) {
        log.info("NOTIFICATION_REQUEST={}", event);

        try {
            notificationClient.send(event);
            log.info(
                    "Welcome notification sent, eventId={}",
                    event.eventId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to send welcome notification, " +
                            "eventId={}",
                    event.eventId(),
                    exception
            );
        }
    }
}