package org.mini_lab.file_upload_service.security.authentication.register.components;

import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InternalServerException;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.security.authentication.register.service.OutboxEventStateManager;
import org.mini_lab.file_upload_service.security.authentication.register.service.OutboxWorker;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {

    private final OutBoxEventRepository outBoxEventRepository;
    private final OutboxWorker outboxWorker;
    private final OutboxEventStateManager outboxEventStateManager;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void polling() {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "created_at");
        List<Long> ids = outBoxEventRepository.findByStatus(pageable);

        for (Long id : ids) {
            try {
                outboxEventStateManager.markProcessing(id);
                outboxWorker.publishEvent(id);
            } catch (InvalidStateTransitionException ex) {
                log.warn("POLLING_CLAIM_JOB FAILED id={} ex={}", id, ex);
            } catch (Exception ex) {
                throw new InternalServerException();
            }
        }
    }


}
