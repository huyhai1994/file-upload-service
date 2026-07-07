package org.mini_lab.file_upload_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FileUploadServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
