package org.mini_lab.file_upload_service.security.authentication.register.service;

import lombok.RequiredArgsConstructor;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxEventStateManager {
    private final OutBoxEventRepository outBoxEventRepository;
    private final Clock clock;
    private static final int MAX_RETRY = 3;

    @Transactional
    void markProcessing(Long id) {
        int claims = outBoxEventRepository.markProcessing(id, Instant.now(clock));
        isValidClaims(claims);
    }

    @Transactional
    public void markComplete(Long id) {
        int claims = outBoxEventRepository.markCompleted(id, Instant.now(clock));
        isValidClaims(claims);
    }

    @Transactional
    public void handlePublishFailure(Long id) {

        OutboxEvent event = outBoxEventRepository.findById(id)
                .orElseThrow();

        int claims;
        if (event.getRetryCount() >= MAX_RETRY) {
            claims = outBoxEventRepository.markFailed(id, Instant.now(clock));
        } else {
            claims = outBoxEventRepository.retryEvent(id, Instant.now(clock));
        }
        isValidClaims(claims);
    }

    private static void isValidClaims(int claims) {
        if (claims == 0) throw new InvalidStateTransitionException();
    }
}
