package com.demo.ai.dto;

/**
 * RAG 检索到的文档来源引用（类型安全，替代 Map&lt;String, Object&gt;）
 */
public record DocSource(String source, String snippet) {
}
