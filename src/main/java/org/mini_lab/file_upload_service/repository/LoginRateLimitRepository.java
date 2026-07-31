package org.mini_lab.file_upload_service.repository;


import org.mini_lab.file_upload_service.entity.LoginRateLimit;
import org.mini_lab.file_upload_service.entity.LoginRateLimitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LoginRateLimitRepository extends JpaRepository<LoginRateLimit, LoginRateLimitId> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO login_rate_limit (
                        identity_hash,
                        window_start,
                        attempt_count
                    )
                    VALUES (
                        :identityHash,
                        :windowStart,
                        1
                    )
                    ON DUPLICATE KEY UPDATE
                        attempt_count = attempt_count + 1
                    """,
            nativeQuery = true
    )
    int incrementCounterAndReturnAffectedRows(
            @Param("identityHash") String identityHash,
            @Param("windowStart") Instant windowStart
    );

    @Query("""
            SELECT rateLimit.attemptCount
            FROM LoginRateLimit  as rateLimit
            WHERE rateLimit.id.indentityHash = :identityHash
              AND rateLimit.id.windowStart = :windowStart
            """)
    Integer findAttemptCount(
            @Param("identityHash") String identityHash,
            @Param("windowStart") Instant windowStart
    );


}
