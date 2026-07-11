package org.mini_lab.file_upload_service.configuration;

import org.junit.jupiter.api.Test;
import org.mini_lab.file_upload_service.support.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MimeTypePropertiesConfigurationTest extends AbstractIntegrationTest {

    @Autowired
    MimeTypePropertiesConfiguration mimeTypePropertiesConfiguration;


    @Test
    void shouldBindAllowedListFromEnvironment() {
        assertNotNull(mimeTypePropertiesConfiguration.getAllowedList());
        assertTrue(mimeTypePropertiesConfiguration.getAllowedList().size() > 1);
    }

}