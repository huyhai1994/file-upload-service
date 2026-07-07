package org.mini_lab.file_upload_service;

import org.springframework.boot.SpringApplication;

public class TestFileUploadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(FileUploadServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
