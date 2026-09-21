CREATE TABLE IF NOT EXISTS `outbox_events`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `event_id`     BINARY(16)   NOT NULL,
    `payload`      TEXT         NOT NULL,
    `status`       VARCHAR(30)  NOT NULL,
    `retry_count`  INTEGER      NOT NULL DEFAULT 0,
    `created_at`   TIMESTAMP(6) NOT NULL,
    `updated_at`   TIMESTAMP(6) NOT NULL,
    `processed_at` TIMESTAMP(6),

    CONSTRAINT `uk_outbox_event_id`
        UNIQUE (`event_id`),

    CONSTRAINT `chk_outbox_retry_count`
        CHECK (`retry_count` >= 0 AND `retry_count` <= 3)
);