package org.mini_lab.file_upload_service.security.rate_limiter.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.mini_lab.file_upload_service.support.RaceConditionSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class RedisCounterRepositoryIntegrationTest extends AbstractIntegrationTest {

    private static final String KEY = "test:counter";

    @Autowired
    private RedisCounterRepository repository;


    @BeforeEach
    void setUp() {
        repository.delete(KEY);
    }

    @AfterEach
    void cleanUp() {
        repository.delete(KEY);
    }


    @Test
    void shouldIncrementCounterAtomically() {
        String key = KEY;

        long first = repository.increment(key);
        long second = repository.increment(key);
        long third = repository.increment(key);

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(2);
        assertThat(third).isEqualTo(3);
    }

    @Test
    void increment_when10ConcurrentRequests_thenCounterReturn10() throws Exception {

        try (RaceConditionSimulator raceConditionSimulator = RaceConditionSimulator.getRaceConditionSimulator(10)) {
            List<Long> results = raceConditionSimulator.execute(() -> repository.increment(KEY));
            assertThat(results).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        }

    }
}