CREATE TABLE IF NOT EXISTS outbox_events
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    exchange     VARCHAR(64)  NOT NULL COMMENT 'RabbitMQ Exchange',
    routing_key  VARCHAR(64)  NOT NULL COMMENT 'RabbitMQ Routing Key',
    payload_type VARCHAR(256) NOT NULL COMMENT 'Java 类全限定名，用于反序列化',
    payload      TEXT         NOT NULL COMMENT '消息内容（JSON）',
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|PUBLISHED|FAILED',
    created_at   DATETIME(6)  NOT NULL,
    published_at DATETIME(6),
    version      BIGINT                DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_status_created (status, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Transactional Outbox 事件表，保证 DB 写入与 MQ 发布的最终一致性';
