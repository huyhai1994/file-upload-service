package org.mini_lab.file_upload_service.security.rate_limiter.filter;

import org.junit.jupiter.api.AfterEach;
import org.mini_lab.file_upload_service.security.rate_limiter.repository.LoginRateLimitRepository;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LoginRateLimiterFilterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    LoginRateLimitRepository loginRateLimitRepository;


    @AfterEach
    void cleanUp() {
        loginRateLimitRepository.deleteAllInBatch();
    }

}