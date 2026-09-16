CREATE TABLE IF NOT EXISTS `outbox_events`
(
    `id`           VARCHAR(255) NOT NULL,
    `payload`      TEXT         NOT NULL,
    `status`       VARCHAR(30)  NOT NULL,
    `retry_count`  INTEGER      NOT NULL,
    `created_at`   TIMESTAMP(6) NOT NULL,
    `processed_at` TIMESTAMP(6),
    PRIMARY KEY (`id`)
);
