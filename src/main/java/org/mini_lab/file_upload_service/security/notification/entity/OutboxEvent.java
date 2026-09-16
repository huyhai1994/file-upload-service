package org.mini_lab.file_upload_service.security.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.mini_lab.file_upload_service.security.notification.dto.OutboxEventStatus;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
@Getter
public class OutboxEvent {

    @Id
    @Column(name = "id", nullable = false, length = 255)
    private String id;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OutboxEventStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    private OutboxEvent(
            String id,
            String payload,
            OutboxEventStatus status,
            Integer retryCount,
            Instant createdAt,
            Instant processedAt
    ) {
        this.id = id;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    public OutboxEvent() {

    }

}