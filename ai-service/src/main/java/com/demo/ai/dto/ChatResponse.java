package com.demo.ai.dto;

import java.util.List;

public record ChatResponse(
		String answer,
		List<DocSource> sources // RAG 检索到的文档来源（类型安全）
) {
	public static ChatResponse of(String answer) {
		return new ChatResponse(answer, List.of());
	}

	public static ChatResponse of(String answer, List<DocSource> sources) {
		return new ChatResponse(answer, sources);
	}
}
