package org.mini_lab.file_upload_service.rate_limiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class LoginRateLimitId implements Serializable {
    @Column(name = "identity_hash",
            nullable = false,
            length = 64
    )
    private String indentityHash;

    @Column(name = "window_start",
            nullable = false)
    private Instant windowStart;
}
