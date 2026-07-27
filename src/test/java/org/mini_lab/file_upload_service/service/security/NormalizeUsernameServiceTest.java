package org.mini_lab.file_upload_service.service.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class NormalizeUsernameServiceTest {

    private final NormalizeUsernameService normalizeUsernameService = new NormalizeUsernameService();

    @Test
    void normalizeUsername_whenNameContaninsUppercaseAndSpacing_thenReturnLowerCase() {
        String uppercaseUsername = "User123 ";
        assertEquals("user123", normalizeUsernameService.normalizeUsername(uppercaseUsername));

    }

}