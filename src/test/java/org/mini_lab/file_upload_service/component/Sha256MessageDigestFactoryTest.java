package org.mini_lab.file_upload_service.component;


import org.junit.jupiter.api.Test;

import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Sha256MessageDigestFactoryTest {
    private final Sha256MessageDigestFactory sha256MessageDigestFactory = new Sha256MessageDigestFactory();

    @Test
    void whenCreateMessageDigest_shouldNotThrownException() {
        assertDoesNotThrow(
                sha256MessageDigestFactory::createMessageDigest
        );
        MessageDigest messageDigest = sha256MessageDigestFactory.createMessageDigest();
        assertEquals("SHA-256", messageDigest.getAlgorithm());
    }


}