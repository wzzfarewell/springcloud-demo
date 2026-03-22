package com.demo.ai.controller;

import com.demo.ai.dto.ChatRequest;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.dto.IngestRequest;
import com.demo.ai.dto.IngestResponse;
import com.demo.ai.service.RagService;
import com.demo.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG（检索增强生成）演示接口
 *
 * <p>
 * 核心流程：
 * 
 * <pre>
 *  文档入库: POST /api/ai/rag/ingest
 *           → 文本切分 → 向量化（EmbeddingModel） → 存入 SimpleVectorStore
 *
 *  RAG 问答: POST /api/ai/rag/chat
 *           → 问题向量化 → 相似度检索 → 上下文组装 → LLM 生成答案
 *
 *  普通问答: POST /api/ai/chat
 *           → 直接发送给 LLM（无知识库上下文，用于效果对比）
 * </pre>
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class RagController {

	private final RagService ragService;

	/**
	 * 文档入库接口
	 * 将文本内容切分并向量化后存入内存向量库
	 */
	@PostMapping("/rag/ingest")
	public Result<IngestResponse> ingestDocument(@Valid @RequestBody IngestRequest request) {
		int chunks = ragService.ingestDocument(request.content(), request.source());
		return Result.success(new IngestResponse(request.source(), "indexed", chunks));
	}

	/**
	 * RAG 问答接口（携带知识库上下文）
	 * 返回 LLM 答案 + 检索到的相关文档来源
	 */
	@PostMapping("/rag/chat")
	public Result<ChatResponse> ragChat(@Valid @RequestBody ChatRequest request) {
		ChatResponse response = ragService.ragChat(request.question());
		return Result.success(response);
	}

	/**
	 * 普通问答接口（无 RAG，直接与 LLM 对话）
	 * 用于与 RAG 问答进行效果对比
	 */
	@PostMapping("/chat")
	public Result<ChatResponse> simpleChat(@Valid @RequestBody ChatRequest request) {
		ChatResponse response = ragService.simpleChat(request.question());
		return Result.success(response);
	}

	/**
	 * 查询当前向量库中已索引的文档片段数量
	 */
	@GetMapping("/rag/stats")
	public Result<Map<String, Object>> stats() {
		int count = ragService.getIndexedDocumentCount();
		return Result.success(Map.of("indexedChunks", count));
	}
}
