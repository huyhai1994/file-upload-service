package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.authentication.register.configurations.NotificationRegisterEventProperties;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
        } catch (KafkaException ex) {
            outboxEventStateManager.handlePublishFailure(id);
            return;
        }
        outboxEventStateManager.markComplete(id);

    }


}
