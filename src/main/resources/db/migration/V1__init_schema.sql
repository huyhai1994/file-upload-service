CREATE TABLE file_metadata
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255),
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    extension    VARCHAR(20)  NOT NULL,
    object_key   VARCHAR(255) NOT NULL,
    bucket       VARCHAR(100) NOT NULL,
    size         BIGINT       NOT NULL,
    checksum     CHAR(64),
    status       VARCHAR(20)  NOT NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL
);