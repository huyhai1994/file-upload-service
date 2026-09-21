package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.security.authentication.register.configurations.NotificationRegisterEventProperties;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.springframework.kafka.KafkaException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletionException;

@Service
@Slf4j
@RequiredArgsConstructor
@Async("notificationTaskExecutor")
public class OutboxWorker {
    private final OutBoxEventRepository outBoxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final OutboxEventStateManager outboxEventStateManager;
    private final NotificationRegisterEventProperties properties;

    public void publishEvent(Long id) {
        OutboxEvent event = outBoxEventRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Outbox event not found: " + id
                        )
                );

        outboxEventStateManager.markProcessing(id);

        try {
            kafkaEventProducer.send(
                    properties.getTopic(),
                    String.valueOf(event.getId()),
                    event.getPayload()
            ).join();
        } catch (KafkaException | CompletionException ex) {
            log.error("PUBLISH_EVENT_FAILED id={} , ex={}", id, ex.getMessage());
            outboxEventStateManager.handlePublishFailure(id);
            return;
        }
        outboxEventStateManager.markComplete(id);

    }


}
