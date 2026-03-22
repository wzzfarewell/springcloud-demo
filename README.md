# Spring Cloud Demo

基于 **Java 21 + Spring Boot 3.3.4 + Spring Cloud 2023.0.3** 的微服务示例项目，涵盖服务注册发现、API 网关、OpenFeign、MySQL、Redis、RabbitMQ 等主流技术栈。

## 项目架构

```
springcloud-demo/
├── common/            公共模块：DTO、统一响应、常量、异常定义
├── eureka-server/     服务注册中心 (port: 8761)
├── gateway-service/   API 网关 + JWT 全局鉴权 (port: 8080)
├── user-service/      用户服务 (port: 8081)  - 注册/登录/JWT 颁发
├── product-service/   商品服务 (port: 8082)  - 商品 CRUD + Redis 缓存
├── order-service/     订单服务 (port: 8083)  - 下单/OpenFeign/AMQP 消息
└── ai-service/        AI 服务  (port: 8084)  - Spring AI RAG 知识库问答
```

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 虚拟线程 (Project Loom) |
| Spring Boot | 3.3.4 | 主框架 |
| Spring Cloud | 2023.0.3 | 微服务生态 |
| Gradle | 9.x | 多模块构建 |
| Spring Cloud Netflix Eureka | 4.1.x | 服务注册与发现 |
| Spring Cloud Gateway | 4.1.x | API 网关 |
| Spring Cloud OpenFeign | 4.1.x | 声明式 HTTP 客户端 |
| Spring Data JPA + Hibernate 6 | — | ORM 持久化 |
| Flyway | — | 数据库版本迁移 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存 |
| RabbitMQ | 3.13 | 消息队列 (AMQP) |
| JWT (jjwt) | 0.12.5 | 认证令牌 |
| Spring AI | 1.0.0 | RAG 知识库问答 |

## 核心功能

- **统一鉴权**：用户登录后获取 JWT，网关层统一校验，通过 `X-User-Id / X-User-Name / X-User-Role` 头透传到下游服务
- **服务发现**：所有服务注册到 Eureka，Gateway 与 Feign 均通过服务名进行负载均衡调用
- **OpenFeign**：`order-service` 通过 FeignClient 调用 `user-service` 和 `product-service`，无需硬编码 URL
- **Redis 缓存**：用户信息缓存 30 分钟，商品信息缓存 1 小时，写操作时自动失效
- **RabbitMQ 消息**：注册成功发 `user.created` 事件；下单成功发 `order.created` 事件；支付后发 `order.paid` 事件
- **Flyway 迁移**：SQL 版本化管理，应用启动时自动执行
- **Java 21 虚拟线程**：所有 Web 服务均开启 `spring.threads.virtual.enabled=true`
- **Spring AI RAG**：`ai-service` 集成 Spring AI 1.0.0，使用内存向量库（SimpleVectorStore）实现检索增强生成：文档入库 → 切分 → 向量化 → 相似度检索 → LLM 组装答案

## 快速启动

### 前置条件

- Java 21+
- Docker & Docker Compose
- Gradle 9.x（或使用项目内 `gradlew`）

### 1. 启动基础服务

```bash
docker-compose up -d
```

等待 MySQL、Redis、RabbitMQ 全部健康后继续。

### 2. 创建数据库（若不使用 Docker）

```sql
CREATE DATABASE springcloud_user   CHARACTER SET utf8mb4;
CREATE DATABASE springcloud_product CHARACTER SET utf8mb4;
CREATE DATABASE springcloud_order   CHARACTER SET utf8mb4;
```

### 3. 修改数据库连接配置

修改各服务 `src/main/resources/application.yml` 中的 `spring.datasource` 配置以匹配你的环境。

### 4. 按顺序启动服务

```bash
# 1. 服务注册中心
gradle :eureka-server:bootRun

# 2. API 网关
gradle :gateway-service:bootRun

# 3. 业务服务（顺序不限）
gradle :user-service:bootRun
gradle :product-service:bootRun
gradle :order-service:bootRun

# 4. AI 服务（需配置 OpenAI API Key）
export OPENAI_API_KEY=sk-your-key     # Linux/macOS
# $env:OPENAI_API_KEY="sk-your-key"  # Windows PowerShell
gradle :ai-service:bootRun

# 也可使用 Ollama 本地模型（免费，无需 API Key）
# 先执行：ollama pull qwen2.5:7b && ollama pull nomic-embed-text
gradle :ai-service:bootRun --args='--spring.profiles.active=ollama'
```

## API 示例

所有请求通过网关 `http://localhost:8080` 访问。

### 注册用户

```http
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
  "username": "alice",
  "password": "pass123",
  "email": "alice@example.com"
}
```

### 登录获取 Token

```http
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "username": "alice",
  "password": "pass123"
}
```

响应：
```json
{ "code": 200, "data": { "token": "eyJ...", "type": "Bearer" } }
```

### 查询商品（需携带 Token）

```http
GET http://localhost:8080/api/products
Authorization: Bearer eyJ...
```

### 下单（需携带 Token）

```http
POST http://localhost:8080/api/orders
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "userId": 1,
  "items": [
    { "productId": 1, "quantity": 2 }
  ],
  "remark": "尽快发货"
}
```

### 更新订单状态

```http
PUT http://localhost:8080/api/orders/1/status?status=PAID
Authorization: Bearer eyJ...
```

## Spring AI RAG 演示

`ai-service` 实现了一个完整的 RAG（Retrieval-Augmented Generation，检索增强生成）流程：

```
文档入库流程:  原始文本 → TokenTextSplitter 切分 → EmbeddingModel 向量化 → SimpleVectorStore 存储
问答流程:      用户提问 → 向量化 → 相似度检索（Top-K）→ 组装 Prompt → ChatModel 生成答案
```

### 文档入库

```http
POST http://localhost:8080/api/ai/rag/ingest
Content-Type: application/json

{
  "content": "你的文档内容...",
  "source": "文档来源标识"
}
```

### RAG 问答（基于知识库）

```http
POST http://localhost:8080/api/ai/rag/chat
Content-Type: application/json

{
  "question": "各个服务分别运行在哪个端口？"
}
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "answer": "根据知识库信息：eureka-server 运行在 8761 端口，gateway-service 在 8080 端口...",
    "sources": [
      { "source": "knowledge-base.md", "snippet": "服务列表与端口..." }
    ]
  }
}
```

### 普通问答（无 RAG，用于效果对比）

```http
POST http://localhost:8080/api/ai/chat
Content-Type: application/json

{
  "question": "这个项目用了哪些技术？"
}
```

> **效果对比**：同一问题分别调用 `/api/ai/rag/chat` 和 `/api/ai/chat`，
> 前者会结合项目知识库给出精准答案，后者则由 LLM 凭通用知识自由发挥。

## 监控入口

| 服务 | Actuator |
|------|----------|
| Eureka Dashboard | http://localhost:8761 |
| Gateway | http://localhost:8080/actuator |
| User Service | http://localhost:8081/actuator/health |
| Product Service | http://localhost:8082/actuator/health |
| Order Service | http://localhost:8083/actuator/health |
| AI Service | http://localhost:8084/actuator/health |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |

## 项目结构说明

```
common/src/main/java/com/demo/common/
├── constant/    RabbitMQ 交换机/队列名、Redis Key 前缀常量
├── dto/         UserDTO、ProductDTO、OrderDTO、OrderItemDTO
├── exception/   BusinessException、ErrorCode
└── response/    Result<T> 统一响应包装

user-service/src/main/java/com/demo/user/
├── config/      SecurityConfig、RedisConfig、RabbitMQConfig
├── controller/  UserController（注册/登录/CRUD）
├── dto/         LoginRequest、RegisterRequest
├── entity/      User（JPA 实体）
├── exception/   GlobalExceptionHandler
├── repository/  UserRepository
└── service/     UserService（接口）、impl/UserServiceImpl、JwtService

product-service/src/main/java/com/demo/product/
├── config/      RedisConfig
├── controller/  ProductController
├── dto/         CreateProductRequest
├── entity/      Product
├── exception/   GlobalExceptionHandler
├── repository/  ProductRepository
└── service/     ProductService（接口）、impl/ProductServiceImpl

order-service/src/main/java/com/demo/order/
├── config/      RabbitMQConfig
├── controller/  OrderController
├── dto/         CreateOrderRequest、OrderItemRequest
├── entity/      Order、OrderItem
├── exception/   GlobalExceptionHandler
├── feign/       UserFeignClient、ProductFeignClient（OpenFeign）
├── mq/          OrderMessageConsumer（AMQP 消费者）
├── repository/  OrderRepository
└── service/     OrderService（接口）、impl/OrderServiceImpl

ai-service/src/main/java/com/demo/ai/
├── config/      AiConfig（VectorStore & TokenTextSplitter Bean）
├── controller/  RagController（/api/ai/** 接口）
├── dto/         ChatRequest、ChatResponse、IngestRequest
├── exception/   GlobalExceptionHandler
├── service/     RagService（RAG 核心逻辑）
└── startup/     KnowledgeBaseLoader（启动时预加载示例文档）

ai-service/src/main/resources/
├── application.yml          端口/Eureka/OpenAI 配置（含 Ollama profile）
└── docs/
    └── knowledge-base.md    预置示例知识库（项目文档）
```
