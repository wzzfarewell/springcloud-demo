CREATE TABLE IF NOT EXISTS products
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description TEXT,
    price       DECIMAL(19, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    category    VARCHAR(50),
    available   TINYINT(1)     NOT NULL DEFAULT 1,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 初始化示例数据
INSERT INTO products (name, description, price, stock, category)
VALUES ('iPhone 15 Pro', '苹果旗舰智能手机，A17 Pro 芯片', 8999.00, 100, '手机'),
       ('MacBook Air M3', '轻薄笔记本，M3 芯片，18小时续航', 9999.00, 50, '电脑'),
       ('AirPods Pro 2', '主动降噪真无线耳机', 1899.00, 200, '耳机'),
       ('iPad Air M2', '轻薄平板电脑', 4799.00, 80, '平板');
