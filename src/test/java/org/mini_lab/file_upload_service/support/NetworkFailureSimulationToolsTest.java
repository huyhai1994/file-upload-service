package org.mini_lab.file_upload_service.support;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Profile("test")
class NetworkFailureSimulationToolsTest extends AbstractIntegrationTest {
    @Test
    void whenClose_thenRemoveToxic() throws IOException {
        NetworkFailureSimulationTools networkFailureSimulationTools = NetworkFailureSimulationTools.applyTo(minioProxy);
        networkFailureSimulationTools.close();
        assertTrue(networkFailureSimulationTools.isRemoved(NetworkFailureSimulationTools.CUT_UPSTREAM));
        assertTrue(networkFailureSimulationTools.isRemoved(NetworkFailureSimulationTools.CUT_DOWNSTREAM));
    }
}
