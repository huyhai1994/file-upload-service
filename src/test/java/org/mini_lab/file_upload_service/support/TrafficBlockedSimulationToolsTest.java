//package org.mini_lab.file_upload_service.support;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Profile;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//@Profile("test")
//class TrafficBlockedSimulationToolsTest extends AbstractIntegrationTest {
//    @Disabled
//    @Test
//    void whenClose_thenRemoveToxic() throws IOException {
//        TrafficBlockedSimulationTools trafficBlockedSimulationTools = TrafficBlockedSimulationTools.applyTo(minioProxy);
//        trafficBlockedSimulationTools.close();
//        assertTrue(trafficBlockedSimulationTools.isRemoved(TrafficBlockedSimulationTools.CUT_UPSTREAM));
//        assertTrue(trafficBlockedSimulationTools.isRemoved(TrafficBlockedSimulationTools.CUT_DOWNSTREAM));
//    }
//}
