package org.mini_lab.file_upload_service.rate_limiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "login_rate_limit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRateLimit {

    @EmbeddedId
    private LoginRateLimitId id;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

}
