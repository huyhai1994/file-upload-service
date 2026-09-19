package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.security.notification.dto.OutboxEventStatus;
import org.mini_lab.file_upload_service.security.notification.dto.UserRegisteredEvent;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OutboxEventDomainPublisher implements DomainEventPublisher {

    private final OutBoxEventRepository outBoxEventRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishEvent(UserRegisteredEvent event) {
        outBoxEventRepository.save(mapFrom(event));
    }

    private OutboxEvent mapFrom(UserRegisteredEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setRetryCount(3);
        outboxEvent.setPayload(event.toString());
        return outboxEvent;
    }


}
