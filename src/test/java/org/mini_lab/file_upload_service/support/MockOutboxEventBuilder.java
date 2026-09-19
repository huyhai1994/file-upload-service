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
                .payload("""
                        {
                          "userId": "user-1",
                          "email": "test1@example.com"
                        }
                        """)
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
    }

    public static OutboxEvent processingEvent() {
        return builder()
                .payload("""
                        {
                          "userId": "user-2",
                          "email": "test2@example.com"
                        }
                        """)
                .status(OutboxEventStatus.PROCESSING)
                .retryCount(0)
                .build();
    }

    public static OutboxEvent completedEvent() {
        return builder()
                .payload("""
                        {
                          "userId": "user-3",
                          "email": "test3@example.com"
                        }
                        """)
                .status(OutboxEventStatus.COMPLETED)
                .retryCount(0)
                .processedAt(Instant.parse("2026-09-16T01:03:00Z"))
                .build();
    }

    public static OutboxEvent failedEvent() {
        return builder()
                .payload("""
                        {
                          "userId": "user-4",
                          "email": "test4@example.com"
                        }
                        """)
                .status(OutboxEventStatus.FAILED)
                .retryCount(3)
                .processedAt(Instant.parse("2026-09-16T01:05:00Z"))
                .build();
    }

    public static final class Builder {

        private String payload = """
                {
                  "userId": "user-default"
                }
                """;

        private OutboxEventStatus status = OutboxEventStatus.PENDING;
        private Integer retryCount = 0;
        private Instant processedAt;

        private Builder() {
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

        public Builder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public OutboxEvent build() {
            OutboxEvent event = new OutboxEvent();

            ReflectionTestUtils.setField(event, "payload", payload);
            ReflectionTestUtils.setField(event, "status", status);
            ReflectionTestUtils.setField(event, "retryCount", retryCount);
            ReflectionTestUtils.setField(event, "processedAt", processedAt);

            return event;
        }
    }
}