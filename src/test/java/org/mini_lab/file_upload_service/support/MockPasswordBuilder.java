package org.mini_lab.file_upload_service.support;

public class MockPasswordBuilder {
    private MockPasswordBuilder() {
    }

    public static final String RAW_PASSWORD = "12345678";
    public static final String VALID_PASSWORD = "password123";
    public static final String PASSWORD_HASH = "$2a$10$encoded-password";
    public static final String WRONG_PASSWORD = "wrong-password";

}
