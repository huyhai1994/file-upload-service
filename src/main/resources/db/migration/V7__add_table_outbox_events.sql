CREATE TABLE IF NOT EXISTS `outbox_events`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `payload`      TEXT         NOT NULL,
    `status`       VARCHAR(30)  NOT NULL,
    `retry_count`  INTEGER DEFAULT 0,
    `created_at`   TIMESTAMP(6) NOT NULL,
    `updated_at`   TIMESTAMP(6) NOT NULL,
    `processed_at` TIMESTAMP(6),
    CHECK ( `retry_count` <= 3)
);
