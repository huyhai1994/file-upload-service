package org.mini_lab.file_upload_service.security.rate_limiter.service;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles({"test"})
class RedisLoginRateLimitServiceIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    RedisLoginRateLimitService service;

    @Test
    void allow_whenTenConcurrentRequests_thenAllowOnlyFive()
            throws Exception {

        String identity =
                "test-identity-" + UUID.randomUUID();

        try (RaceConditionSimulator simulator =
                     RaceConditionSimulator
                             .getRaceConditionSimulator(10)) {

            List<Boolean> results = simulator.execute(
                    () -> service.allow(identity)
            );

            assertThat(results)
                    .hasSize(10);

            assertThat(results)
                    .filteredOn(Boolean.TRUE::equals)
                    .hasSize(5);

            assertThat(results)
                    .filteredOn(Boolean.FALSE::equals)
                    .hasSize(5);
        }
    }
}