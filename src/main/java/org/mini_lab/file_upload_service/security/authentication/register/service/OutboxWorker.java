package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxWorker {
    private final OutBoxEventRepository outBoxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public void publishEvent(UUID id) {


    }


}
