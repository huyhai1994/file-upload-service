package org.mini_lab.file_upload_service.configuration;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ExtensionConfigurationsTest extends AbstractIntegrationTest {


    @Autowired
    ExtensionConfigurations extensionConfigurations;

    @Test
    void shouldBindAllowedListFromEnvironment() {
        assertNotNull(extensionConfigurations.getAllowedList());
        assertTrue(extensionConfigurations.getAllowedList().size() > 1);
    }


}