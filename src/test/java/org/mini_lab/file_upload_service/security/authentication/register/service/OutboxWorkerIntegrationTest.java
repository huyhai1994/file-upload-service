package org.mini_lab.file_upload_service.security.authentication.register.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.file_upload.shared.exception.InvalidStateTransitionException;
import org.mini_lab.file_upload_service.security.notification.dto.OutboxEventStatus;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.security.notification.repository.OutBoxEventRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockOutboxEventBuilder;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class OutboxWorkerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    OutboxWorker outboxWorker;

    @MockitoSpyBean
    OutBoxEventRepository outBoxEventRepository;

    @MockitoSpyBean
    KafkaEventProducer kafkaEventProducer;

    @MockitoSpyBean
    OutboxEventStateManager outboxEventStateManager;


    @AfterEach
    void cleanUp() {
        outBoxEventRepository.deleteAllInBatch();
    }

    @Test
    void publishEvent_whenKafkaEventPublished_thenMarkCompleted() {
        Long id = outBoxEventRepository.save(MockOutboxEventBuilder.pendingEvent()).getId();


        outboxWorker.publishEvent(id);

        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OutboxEvent event = outBoxEventRepository.findById(id).orElseThrow();
                    assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.COMPLETED);
                });
    }

    @Test
    void publishEvent_whenMultipleRequestConcurrence_OnlyOneMessageBeSent() throws ExecutionException, InterruptedException, TimeoutException {
        Long id = outBoxEventRepository.save(MockOutboxEventBuilder.pendingEvent()).getId();
        AtomicInteger failureCount = new AtomicInteger(0);

        try (RaceConditionSimulator raceConditionSimulator = RaceConditionSimulator.getRaceConditionSimulator(10)) {
            raceConditionSimulator.execute(
                    () -> {
                        try {
                            outboxWorker.publishEvent(id);
                        } catch (InvalidStateTransitionException e) {
                            failureCount.getAndIncrement();
                        }
                        return null;
                    }
            );
        }

        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(failureCount.get()).isEqualTo(9);
                    OutboxEvent event = outBoxEventRepository.findById(id).orElseThrow();
                    verify(kafkaEventProducer, times(1)).send(anyString(), anyString(), anyString());
                    assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.COMPLETED);
                });
    }

    @Test
    void publishEvent_whenKafkaEventFailed_thenRetry() throws IOException {
        Long id = outBoxEventRepository.save(MockOutboxEventBuilder.pendingEvent()).getId();

        kafka.stop();

        outboxWorker.publishEvent(id);

        Awaitility
                .await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OutboxEvent event = outBoxEventRepository.findById(id).orElseThrow();
                    assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
                    verify(outboxEventStateManager).handlePublishFailure(id);
                });
    }

}