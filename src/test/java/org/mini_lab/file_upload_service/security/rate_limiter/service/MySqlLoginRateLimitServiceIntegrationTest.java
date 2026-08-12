package org.mini_lab.file_upload_service.security.rate_limiter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.LoginRateLimitRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mini_lab.file_upload_service.support.MockLoginRequestBuilder.IDENTITY_HASH;
import static org.mini_lab.file_upload_service.support.RaceConditionSimulator.getRaceConditionSimulator;

@SpringBootTest
@ActiveProfiles("test")
class MySqlLoginRateLimitServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    MySqlLoginRateLimitService mySqlLoginRateLimitService;

    @Autowired
    LoginRateLimitRepository loginRateLimitRepository;

    @AfterEach
    void cleanUp() {
        loginRateLimitRepository.deleteAllInBatch();
    }

    @Test
    void allow_whenEleventRequestsLoginConcurency_thenReturnFalse() throws ExecutionException, InterruptedException, TimeoutException {

        int concurrentRequestCount = 11;
        int maxAttempts = 10;


        try (RaceConditionSimulator simulator =
                     getRaceConditionSimulator(concurrentRequestCount)) {

            List<Boolean> results = simulator.execute(
                    () -> mySqlLoginRateLimitService.allow(IDENTITY_HASH)
            );

            assertThat(results)
                    .hasSize(concurrentRequestCount);

            assertThat(results)
                    .filteredOn(Boolean.TRUE::equals)
                    .hasSize(maxAttempts);

            assertThat(results)
                    .filteredOn(Boolean.FALSE::equals)
                    .hasSize(concurrentRequestCount - maxAttempts);
        }
    }


}