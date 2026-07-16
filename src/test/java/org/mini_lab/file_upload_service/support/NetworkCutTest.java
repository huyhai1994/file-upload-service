package org.mini_lab.file_upload_service.support;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Profile("test")
class NetworkCutTest extends AbstractIntegrationTest {
    @Test
    void whenClose_thenRemoveToxic() throws IOException {
        NetworkCut networkCut = NetworkCut.applyTo(minioProxy);
        networkCut.close();
        assertTrue(networkCut.isRemoved(NetworkCut.CUT_UPSTREAM));
        assertTrue(networkCut.isRemoved(NetworkCut.CUT_DOWNSTREAM));
    }
}
