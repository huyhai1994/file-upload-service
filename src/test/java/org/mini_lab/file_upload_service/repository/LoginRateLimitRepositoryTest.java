package org.mini_lab.file_upload_service.repository;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.LoginRateLimitRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
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
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mini_lab.file_upload_service.support.MockLoginRequestBuilder.IDENTITY_HASH;
import static org.mini_lab.file_upload_service.support.RaceConditionSimulator.getRaceConditionSimulator;

@ActiveProfiles("test")
@DataJpaTest
@Import(TestClockConfiguration.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class LoginRateLimitRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private LoginRateLimitRepository loginRateLimitRepository;

    @Autowired
    private Clock fixedClock;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void incrementCounter_whenFiveThreadsIncrementConcurrently_thenFinalCounterIsFive()
            throws Exception {

        Instant windowStart = fixedClock.instant()
                .truncatedTo(ChronoUnit.MINUTES);

        try (RaceConditionSimulator simulator =
                     getRaceConditionSimulator(5)) {

            List<Integer> affectedRows = simulator.execute(
                    () -> performIncrement(windowStart)
            );

            assertThat(affectedRows)
                    .containsExactlyInAnyOrder(1, 2, 2, 2, 2);

            int persistedCounter =
                    loginRateLimitRepository.findAttemptCount(
                            IDENTITY_HASH,
                            windowStart
                    );

            assertThat(persistedCounter).isEqualTo(5);
        }
    }


    public int performIncrement(
            Instant windowStart
    ) {
        return transactionTemplate.execute(status ->

                loginRateLimitRepository.incrementCounterAndReturnAffectedRows(
                        IDENTITY_HASH,
                        windowStart
                )
        );
    }

}