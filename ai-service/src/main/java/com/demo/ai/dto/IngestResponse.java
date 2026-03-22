package com.demo.ai.dto;

/**
 * 文档入库接口的响应体
 */
public record IngestResponse(String source, String status, int chunks) {
}
