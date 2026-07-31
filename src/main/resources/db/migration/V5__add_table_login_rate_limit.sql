CREATE TABLE login_rate_limit
(
    identity_hash VARCHAR(64)  NOT NULL,
    window_start  TIMESTAMP(6) NOT NULL,
    attempt_count INT          NOT NULL,

    PRIMARY KEY (identity_hash, window_start)
);
