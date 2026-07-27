CREATE TABLE users
(
    id                 BINARY(16)   NOT NULL,
    username           VARCHAR(100) NOT NULL,
    password_hash      VARCHAR(255) NOT NULL,
    status             VARCHAR(30)  NOT NULL,
    failed_login_count INT          NOT NULL DEFAULT 0,
    locked_until       DATETIME(6)  NULL,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);