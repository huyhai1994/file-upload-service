package org.mini_lab.file_upload_service.security.rate_limiter.properties;


import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.identity-hash")
public record IdentityHashProperties(String secretKey) {
    public IdentityHashProperties {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException(
                    "security.identity-hash.secret must not be blank"
            );
        }
    }
}
