package org.mini_lab.file_upload_service.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.rate_limiter.repository.LoginRateLimitRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.TestClockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import(TestClockConfiguration.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class LoginRateLimitRepositoryTest extends AbstractIntegrationTest {

    private static final int CONCURRENT_REQUEST_COUNT = 5;
    private static final String IDENTITY_HASH = "test-identity-hash";

    private ExecutorService executorService;

    @Autowired
    private LoginRateLimitRepository loginRateLimitRepository;

    @Autowired
    private Clock fixedClock;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(
                CONCURRENT_REQUEST_COUNT
        );
    }

    @AfterEach
    void cleanUp() throws InterruptedException {
        executorService.shutdownNow();

        boolean terminated = executorService.awaitTermination(
                5,
                TimeUnit.SECONDS
        );

        assertThat(terminated).isTrue();
    }

    @Test
    void incrementCounter_whenFiveThreadsIncrementConcurrently_thenFinalCounterIsFive()
            throws InterruptedException, ExecutionException, TimeoutException {

        Instant windowStart = fixedClock.instant().truncatedTo(ChronoUnit.MINUTES);

        CountDownLatch readyLatch =
                new CountDownLatch(CONCURRENT_REQUEST_COUNT);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<CompletableFuture<Integer>> futures =
                createConcurrentIncrementRequests(
                        IDENTITY_HASH,
                        windowStart,
                        readyLatch,
                        startLatch
                );

        assertThat(
                readyLatch.await(5, TimeUnit.SECONDS)
        ).isTrue();

        startLatch.countDown();

        CompletableFuture.allOf(
                futures.toArray(CompletableFuture[]::new)
        ).get(10, TimeUnit.SECONDS);

        List<Integer> affectiveRows = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        assertThat(affectiveRows)
                .containsExactlyInAnyOrder(1, 2, 2, 2, 2);

        int persistedCounter =
                loginRateLimitRepository.findAttemptCount(
                        IDENTITY_HASH,
                        windowStart
                );

        assertThat(persistedCounter).isEqualTo(5);
    }

    private List<CompletableFuture<Integer>> createConcurrentIncrementRequests(
            String identityHash,
            Instant windowStart,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) {
        return IntStream.range(0, CONCURRENT_REQUEST_COUNT)
                .mapToObj(index ->
                        CompletableFuture.supplyAsync(
                                () -> {
                                    readyLatch.countDown();

                                    awaitStartSignal(startLatch);

                                    try {
                                        return performIncrement(
                                                identityHash,
                                                windowStart
                                        );
                                    } catch (Exception exception) {
                                        throw new IllegalStateException(
                                                "Login counter increment failed",
                                                exception
                                        );
                                    }
                                },
                                executorService
                        )
                )
                .toList();
    }

    private int performIncrement(
            String identityHash,
            Instant windowStart
    ) {
        return transactionTemplate.execute(status ->

                loginRateLimitRepository.incrementCounterAndReturnAffectedRows(
                        identityHash,
                        windowStart
                )
        );
    }

    private void awaitStartSignal(CountDownLatch startLatch) {
        try {
            startLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Concurrent login request was interrupted",
                    exception
            );
        }
    }
}