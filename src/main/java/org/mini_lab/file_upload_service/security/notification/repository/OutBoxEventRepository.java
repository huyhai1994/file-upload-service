package org.mini_lab.file_upload_service.security.notification.repository;

import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, Long> {

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
                       @Param("now") Instant now);

    @Modifying
    @Query("""
            update OutboxEvent oe
            set
                        oe.status = OutboxEventStatus.PENDING,
                        oe.retryCount = oe.retryCount  + 1,
                        oe.updatedAt = :now
            where
                        oe.id = :id
                        and oe.status = OutboxEventStatus.PROCESSING
            """)
    int retryEvent(@Param("id") Long id,
                   @Param("now") Instant now);

    @Modifying
    @Query("""
            update OutboxEvent oe
            set
                        oe.status = OutboxEventStatus.FAILED,
                        oe.updatedAt = :now
            where
                        oe.id = :id
                        and oe.status = OutboxEventStatus.PROCESSING
            """)
    int markFailed(@Param("id") Long id,
                       @Param("now") Instant now);

    @Modifying
    @Query("""
            update OutboxEvent oe
            set
                        oe.status = OutboxEventStatus.COMPLETED,
                        oe.updatedAt = :now,
                        oe.processedAt = :now
            where
                        oe.id = :id
                        and oe.status = OutboxEventStatus.PROCESSING
            """)
    int markCompleted(@Param("id") Long id,
                   @Param("now") Instant now);
}
