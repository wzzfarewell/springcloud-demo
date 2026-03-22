package com.demo.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG 流水线参数，通过 application.yml 中的 app.rag.* 配置
 */
@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(int topK, int chunkSize, int chunkOverlap) {
}
