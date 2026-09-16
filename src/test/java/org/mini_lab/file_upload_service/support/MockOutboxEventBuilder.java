package org.mini_lab.file_upload_service.support;

import org.mini_lab.file_upload_service.security.notification.dto.OutboxEventStatus;
import org.mini_lab.file_upload_service.security.notification.entity.OutboxEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

public final class MockOutboxEventBuilder {

    private MockOutboxEventBuilder() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OutboxEvent pendingEvent() {
        return builder()
                .id("event-1")
                .payload("""
                        {
                          "userId": "user-1",
                          "email": "test1@example.com"
                        }
                        """)
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.parse("2026-09-16T01:00:00Z"))
                .build();
    }

    public static OutboxEvent processingEvent() {
        return builder()
                .id("event-2")
                .payload("""
                        {
                          "userId": "user-2",
                          "email": "test2@example.com"
                        }
                        """)
                .status(OutboxEventStatus.PROCESSING)
                .retryCount(0)
                .createdAt(Instant.parse("2026-09-16T01:01:00Z"))
                .build();
    }

    public static OutboxEvent completedEvent() {
        return builder()
                .id("event-3")
                .payload("""
                        {
                          "userId": "user-3",
                          "email": "test3@example.com"
                        }
                        """)
                .status(OutboxEventStatus.COMPLETED)
                .retryCount(0)
                .createdAt(Instant.parse("2026-09-16T01:02:00Z"))
                .processedAt(Instant.parse("2026-09-16T01:03:00Z"))
                .build();
    }

    public static OutboxEvent failedEvent() {
        return builder()
                .id("event-4")
                .payload("""
                        {
                          "userId": "user-4",
                          "email": "test4@example.com"
                        }
                        """)
                .status(OutboxEventStatus.FAILED)
                .retryCount(3)
                .createdAt(Instant.parse("2026-09-16T01:04:00Z"))
                .processedAt(Instant.parse("2026-09-16T01:05:00Z"))
                .build();
    }

    public static final class Builder {

        private String id = "event-default";
        private String payload = """
                {
                  "userId": "user-default"
                }
                """;
        private OutboxEventStatus status = OutboxEventStatus.PENDING;
        private Integer retryCount = 0;
        private Instant createdAt = Instant.parse("2026-09-16T00:00:00Z");
        private Instant processedAt;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder status(OutboxEventStatus status) {
            this.status = status;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public OutboxEvent build() {
            OutboxEvent event = new OutboxEvent();

            ReflectionTestUtils.setField(event, "id", id);
            ReflectionTestUtils.setField(event, "payload", payload);
            ReflectionTestUtils.setField(event, "status", status);
            ReflectionTestUtils.setField(event, "retryCount", retryCount);
            ReflectionTestUtils.setField(event, "createdAt", createdAt);
            ReflectionTestUtils.setField(event, "processedAt", processedAt);

            return event;
        }
    }
}