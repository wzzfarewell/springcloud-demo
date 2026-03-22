package com.demo.ai.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.demo.ai.config.RagProperties;
import com.demo.ai.dto.ChatResponse;
import com.demo.ai.dto.DocSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RagService {

	// 用户消息模板：将检索到的上下文与问题一起发给 LLM
	private static final String RAG_USER_TEMPLATE = """
			请根据以下上下文回答问题。

			上下文：
			%s

			问题：%s
			""";

	private final SimpleVectorStore vectorStore;
	private final TokenTextSplitter tokenTextSplitter;
	private final ChatClient ragChatClient;
	private final ChatClient simpleChatClient;
	private final RagProperties properties;
	private final AtomicInteger indexedChunkCount = new AtomicInteger(0);

	public RagService(
			SimpleVectorStore vectorStore,
			TokenTextSplitter tokenTextSplitter,
			@Qualifier("ragChatClient") ChatClient ragChatClient,
			@Qualifier("simpleChatClient") ChatClient simpleChatClient,
			RagProperties properties) {
		this.vectorStore = vectorStore;
		this.tokenTextSplitter = tokenTextSplitter;
		this.ragChatClient = ragChatClient;
		this.simpleChatClient = simpleChatClient;
		this.properties = properties;
	}

	/**
	 * 文档入库：文本切分 → 向量化 → 存入内存向量库
	 *
	 * @return 切分生成的 chunk 数量
	 */
	public int ingestDocument(String content, String sourceName) {
		Document doc = new Document(content, Map.of("source", sourceName));
		List<Document> chunks = tokenTextSplitter.apply(List.of(doc));
		vectorStore.add(chunks);
		indexedChunkCount.addAndGet(chunks.size());
		log.info("文档 [{}] 已索引，共切分为 {} 个 chunk", sourceName, chunks.size());
		return chunks.size();
	}

	/**
	 * RAG 问答：单次向量检索 → 组装上下文 → LLM 生成答案
	 * <p>
	 * 相比使用 QuestionAnswerAdvisor，手动 RAG 避免了双重向量检索开销，
	 * 同时使检索结果可直接用于构造 sources 响应字段。
	 */
	public ChatResponse ragChat(String question) {
		List<Document> relatedDocs = vectorStore.similaritySearch(
				SearchRequest.builder().query(question).topK(properties.topK()).build());

		String context = relatedDocs.isEmpty()
				? "暂无相关上下文"
				: relatedDocs.stream()
						.map(Document::getText)
						.collect(Collectors.joining("\n\n---\n\n"));

		String answer = ragChatClient.prompt()
				.user(RAG_USER_TEMPLATE.formatted(context, question))
				.call()
				.content();

		List<DocSource> sources = relatedDocs.stream()
				.map(doc -> new DocSource(
						(String) doc.getMetadata().getOrDefault("source", "unknown"),
						truncate(doc.getText(), 200)))
				.toList();

		return ChatResponse.of(answer, sources);
	}

	/**
	 * 普通问答：无 RAG，直接与 LLM 对话（效果对比用）
	 */
	public ChatResponse simpleChat(String question) {
		String answer = simpleChatClient.prompt()
				.user(question)
				.call()
				.content();
		return ChatResponse.of(answer);
	}

	public int getIndexedDocumentCount() {
		return indexedChunkCount.get();
	}

	private static String truncate(String text, int maxLength) {
		if (text == null)
			return "";
		return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
	}
}
