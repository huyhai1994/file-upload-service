package org.mini_lab.file_upload_service.security.notification.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.MockOutboxEventBuilder;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.mini_lab.file_upload_service.support.TestClockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestClockConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutBoxEventRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutBoxEventRepository outBoxEventRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    Clock clock;

    @AfterEach
    void cleanUp() {
        outBoxEventRepository.deleteAllInBatch();
    }


    @Test
    void save_whenSaveEvent_thenSuccess() {
        OutboxEvent event = outBoxEventRepository.saveAndFlush(MockOutboxEventBuilder.pendingEvent());
        Long id = event.getId();

        entityManager.clear();
        OutboxEvent persistedEvent = outBoxEventRepository.findById(id).orElseThrow();
        assertThat(persistedEvent.getId()).isEqualTo(id);

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void markProcessing_whenMultipleThreadsAccessConcurrently_thenOnlyOneJobClaimed() {
        Long id = outBoxEventRepository.saveAndFlush(MockOutboxEventBuilder.pendingEvent()).getId();
        assertClaimsJobs(
                () -> transactionTemplate.execute(
                        status -> outBoxEventRepository.markProcessing(id, Instant.now(clock))
                ), claim -> claim > 0);

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void retryEvent_whenMultipleThreadsAccessConcurrently_thenOnlyOneJobClaimed() {
        Long id = outBoxEventRepository.saveAndFlush(MockOutboxEventBuilder.processingEvent()).getId();
        assertClaimsJobs(
                () -> transactionTemplate.execute(
                        status -> outBoxEventRepository.retryEvent(id, Instant.now(clock))
                ), claim -> claim > 0);

        OutboxEvent retryEvent = outBoxEventRepository.findById(id).orElseThrow();
        assertThat(retryEvent.getRetryCount()).isOne();

    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void markFailed_whenMultipleThreadsAccessConcurrently_thenOnlyOneJobClaimed() {
        Long id = outBoxEventRepository.saveAndFlush(MockOutboxEventBuilder.processingEvent()).getId();
        assertClaimsJobs(
                () -> transactionTemplate.execute(
                        status -> outBoxEventRepository.markFailed(id, Instant.now(clock))
                ), claim -> claim > 0);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void markCompleted_whenMultipleThreadsAccessConcurrently_thenOnlyOneJobClaimed() {
        Long id = outBoxEventRepository.saveAndFlush(MockOutboxEventBuilder.processingEvent()).getId();
        assertClaimsJobs(
                () -> transactionTemplate.execute(
                        status -> outBoxEventRepository.markCompleted(id, Instant.now(clock))
                ), claim -> claim > 0);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByStatus_Pending_whenThereIsAnPendingJob_thenReturnIt() {
        long id = outBoxEventRepository.save(MockOutboxEventBuilder.pendingEvent()).getId();
        outBoxEventRepository.save(MockOutboxEventBuilder.completedEvent());
        outBoxEventRepository.save(MockOutboxEventBuilder.processingEvent());
        outBoxEventRepository.save(MockOutboxEventBuilder.failedEvent());

        List<Long> foundIds = outBoxEventRepository.findByStatus(PageRequest.of(0, 1));

        assertThat(foundIds.stream().count()).isOne();
        assertThat(foundIds).containsExactly(id);
    }

    private <T> void assertClaimsJobs(Callable<T> callable, Predicate<T> predicate) {
        final int REQUEST_COUNT = 10;

        List<T> claims;

        try (RaceConditionSimulator race = RaceConditionSimulator.getRaceConditionSimulator(REQUEST_COUNT)) {
            try {
                claims = race.execute(callable);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }

        }
        assertThat(
                claims
                        .stream()
                        .filter(predicate)
                        .count())
                .isOne();

    }

}