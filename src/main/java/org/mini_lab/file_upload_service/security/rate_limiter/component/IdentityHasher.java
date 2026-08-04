package org.mini_lab.file_upload_service.security.rate_limiter.component;

import lombok.extern.slf4j.Slf4j;
import org.mini_lab.file_upload_service.security.rate_limiter.properties.IdentityHashProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

@Component
@Slf4j
public class IdentityHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKey;

    public IdentityHasher(IdentityHashProperties properties) {
        byte[] secretBytes = properties.secretKey()
                .getBytes(StandardCharsets.UTF_8);

        this.secretKey = new SecretKeySpec(
                secretBytes,
                HMAC_ALGORITHM
        );
    }

    public String hash(String normalizedIpAddress) {


        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);

            byte[] digest = mac.doFinal(
                    normalizedIpAddress.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException exception) {
            log.error("IDENTITY_HASH ERROR error={}", exception.getMessage());
            throw new IllegalStateException();
        }
    }
}
