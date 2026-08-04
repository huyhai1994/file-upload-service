package org.mini_lab.file_upload_service.security.rate_limiter.component;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.IP_ADDRESS;
import static org.mini_lab.file_upload_service.support.MockUserBuilder.NORMALIZED_USERNAME;

@SpringBootTest
@ActiveProfiles("test")
class IdentityHasherIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    IdentityHasher identityHasher;

    @Test
    void hash_whenUsernameIpValid_thenReturnIdentityHash() {

        String identityHash = identityHasher.hash(IP_ADDRESS);

        assertThat(identityHash).isNotNull();
        assertThat(identityHash).isNotBlank();
        assertThat(identityHash.length()).isEqualTo(64);
        assertThat(identityHash).isNotEqualTo(NORMALIZED_USERNAME + "|" + IP_ADDRESS);
    }


}