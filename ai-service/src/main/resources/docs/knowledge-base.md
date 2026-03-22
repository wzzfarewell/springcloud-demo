# SpringCloud Demo 项目知识库

## 项目概述

SpringCloud Demo 是一个基于 Java 21 + Spring Boot 3.3.4 + Spring Cloud 2023.0.3 的微服务示例项目。
项目展示了微服务架构中的核心技术：服务注册发现、API 网关、声明式 HTTP 客户端、缓存、消息队列和统一鉴权。

## 服务列表与端口

- **eureka-server**（端口 8761）：服务注册中心，所有微服务启动后注册到此，可通过 http://localhost:8761 查看 Eureka Dashboard。
- **gateway-service**（端口 8080）：API 网关，所有外部请求的统一入口，负责 JWT 鉴权和路由转发。
- **user-service**（端口 8081）：用户服务，提供注册、登录、用户信息查询接口，颁发 JWT Token。
- **product-service**（端口 8082）：商品服务，提供商品 CRUD 接口，使用 Redis 缓存热点数据。
- **order-service**（端口 8083）：订单服务，通过 OpenFeign 调用用户服务和商品服务，下单后发送 RabbitMQ 消息。
- **ai-service**（端口 8084）：AI 服务，提供基于 RAG 的知识库问答接口（本服务）。

## 技术栈详解

### Spring Cloud Netflix Eureka
用于服务注册与发现。各服务启动时自动注册到 Eureka Server，Gateway 和 OpenFeign 均通过服务名进行负载均衡调用，无需硬编码 IP 地址。

### Spring Cloud Gateway
API 网关基于 Spring WebFlux（响应式）。主要职责：
1. JWT 全局鉴权过滤器：从 Authorization 头提取并验证 JWT
2. 路由转发：将 /api/users/** 转发到 user-service，/api/products/** 转发到 product-service 等
3. 透传用户信息：验证通过后，将 X-User-Id、X-User-Name、X-User-Role 注入请求头传递给下游服务

### OpenFeign（声明式 HTTP 客户端）
order-service 使用 @FeignClient 定义接口，自动实现服务间 HTTP 调用：
- UserFeignClient：调用 user-service 获取用户信息
- ProductFeignClient：调用 product-service 获取商品信息和扣减库存

### Redis 缓存
- user-service：用户信息缓存 30 分钟（Key 前缀：user:info:）
- product-service：商品信息缓存 1 小时（Key 前缀：product:info:）
- 写操作（更新/删除）时自动删除对应缓存，保证缓存一致性

### RabbitMQ 消息队列
- 用户注册成功：发布 user.created 事件到 user.events 交换机
- 下单成功：发布 order.created 事件到 order.events 交换机
- 支付成功：发布 order.paid 事件，触发库存扣减和通知

### JWT 认证
- jjwt 0.12.5 实现：用户登录后生成包含 userId、username、role 的 JWT Token
- Token 有效期默认 24 小时，密钥通过 jwt.secret 配置
- 网关统一校验，合法 Token 才能访问受保护资源；/api/users/register 和 /api/users/login 为白名单路径

### MySQL + Flyway
- 三个独立数据库：springcloud_user、springcloud_product、springcloud_order
- Flyway 版本化管理 SQL 文件，放置在 classpath:db/migration 目录，应用启动时自动执行

### Java 21 虚拟线程
所有 Web 服务均配置 spring.threads.virtual.enabled=true，使用 Project Loom 虚拟线程处理请求，在高并发 IO 场景下性能更优。

## API 使用说明

### 注册用户
POST http://localhost:8080/api/users/register
请求体：{"username": "alice", "password": "pass123", "email": "alice@example.com"}

### 用户登录（获取 JWT Token）
POST http://localhost:8080/api/users/login
请求体：{"username": "alice", "password": "pass123"}
响应：{"code": 200, "data": {"token": "eyJ...", "type": "Bearer"}}

### 查询商品列表（需要 JWT）
GET http://localhost:8080/api/products
请求头：Authorization: Bearer eyJ...

### 创建商品（需要 JWT）
POST http://localhost:8080/api/products
请求体：{"name": "iPhone 16", "price": 5999.00, "stock": 100, "description": "最新款手机"}

### 下订单（需要 JWT）
POST http://localhost:8080/api/orders
请求体：{"userId": 1, "items": [{"productId": 1, "quantity": 2}], "remark": "尽快发货"}

### 更新订单状态（支付）
PUT http://localhost:8080/api/orders/1/status?status=PAID

## RAG 知识库问答

### 文档入库
POST http://localhost:8080/api/ai/rag/ingest
请求体：{"content": "文档内容...", "source": "文档来源标识"}

### RAG 问答（结合知识库回答）
POST http://localhost:8080/api/ai/rag/chat
请求体：{"question": "项目使用了哪些技术？"}

### 普通问答（直接与 LLM 对话，用于对比）
POST http://localhost:8080/api/ai/chat
请求体：{"question": "什么是微服务？"}

### 查询向量库统计信息
GET http://localhost:8080/api/ai/rag/stats

## 快速启动步骤

1. 启动基础设施：docker-compose up -d（MySQL + Redis + RabbitMQ）
2. 启动注册中心：gradle :eureka-server:bootRun
3. 启动 API 网关：gradle :gateway-service:bootRun
4. 启动业务服务：gradle :user-service:bootRun、:product-service:bootRun、:order-service:bootRun
5. 启动 AI 服务：配置 OPENAI_API_KEY 后运行 gradle :ai-service:bootRun

## 常见问题

Q: AI 服务返回 "AI 服务暂时不可用" 是什么原因？
A: 通常是 API Key 未配置或无效。请在 application.yml 中设置 spring.ai.openai.api-key 或通过环境变量 OPENAI_API_KEY 传入。

Q: 如何不使用 OpenAI，改用本地模型？
A: 使用 Ollama 启动 profile：先安装 Ollama 并拉取模型（ollama pull qwen2.5:7b 和 ollama pull nomic-embed-text），然后以 --spring.profiles.active=ollama 启动 ai-service。

Q: 向量库中的数据重启后会丢失吗？
A: 是的，SimpleVectorStore 是内存向量库，重启后数据清空。应用启动时会自动重新加载 knowledge-base.md。如需持久化，可替换为 ChromaVectorStore 或 PgVectorStore。

Q: 如何提升 RAG 回答质量？
A: 1) 上传更多高质量文档；2) 调整 TokenTextSplitter 的 chunk 大小；3) 增大 SearchRequest.withTopK() 的值；4) 改善 system prompt 中的指令。
