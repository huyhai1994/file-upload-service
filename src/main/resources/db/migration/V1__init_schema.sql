CREATE TABLE file_metadata
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255),
    file_name    VARCHAR(255) not null,
    content_type VARCHAR(100) not null,
    extension    VARCHAR(20),
    object_key   VARCHAR(255) not null,
    size         BIGINT       not null,
    checksum     CHAR(64),
    status       VARCHAR(20)  not null,
    created_at   DATETIME     not null,
    updated_at   DATETIME
);