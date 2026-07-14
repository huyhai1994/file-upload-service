package org.mini_lab.file_upload_service.component;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Sha256MessageDigestFactory implements MessageDigestFactory {

    @Override
    public MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    e
            );
        }
    }
}
