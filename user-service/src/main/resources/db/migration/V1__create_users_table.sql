CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    enabled    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
