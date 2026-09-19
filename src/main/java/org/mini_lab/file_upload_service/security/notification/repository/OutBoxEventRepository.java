package org.mini_lab.file_upload_service.security.notification.repository;

import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // @TODO: PENDING -> PROCESSING
    @Modifying
    @Query("""
            update OutboxEvent oe
            set
                        oe.status = OutboxEventStatus.PROCESSING,
                        oe.updatedAt = :now
            where
                        oe.id = :id
                        and oe.status = OutboxEventStatus.PENDING
            """)
    int markProcessing(@Param("id") Long id,
                       @Param("now")Instant now);

    // @TODO: PROCESSING -> PENDING & update retry_count + 1
    // @TODO: PROCESSING -> FAILED
    // @TODO: PROCESSING -> COMPLETED
}
