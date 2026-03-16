CREATE TABLE IF NOT EXISTS orders
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(32)    NOT NULL UNIQUE,
    user_id      BIGINT         NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    remark       VARCHAR(500),
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(100)   NOT NULL,
    quantity     INT            NOT NULL,
    price        DECIMAL(19, 2) NOT NULL,
    subtotal     DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
