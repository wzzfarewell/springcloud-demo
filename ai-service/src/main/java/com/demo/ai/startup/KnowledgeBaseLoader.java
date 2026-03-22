package com.demo.ai.startup;

import com.demo.ai.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 应用启动后自动加载示例知识库文档
 */
@Slf4j
@Component
public class KnowledgeBaseLoader {

	private final RagService ragService;
	private final Resource knowledgeBaseResource;

	// 构造方法注入：保持字段 final，优于 @Value 字段注入
	public KnowledgeBaseLoader(RagService ragService,
			@Value("classpath:docs/knowledge-base.md") Resource knowledgeBaseResource) {
		this.ragService = ragService;
		this.knowledgeBaseResource = knowledgeBaseResource;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void loadKnowledgeBase() {
		try {
			String content = knowledgeBaseResource.getContentAsString(StandardCharsets.UTF_8);
			int chunks = ragService.ingestDocument(content, "knowledge-base.md");
			log.info("=== 示例知识库已预加载完成，共 {} 个 chunk ===", chunks);
		} catch (IOException e) {
			log.warn("示例知识库预加载失败（可通过 POST /api/ai/rag/ingest 手动添加文档）: {}", e.getMessage());
		}
	}
}
