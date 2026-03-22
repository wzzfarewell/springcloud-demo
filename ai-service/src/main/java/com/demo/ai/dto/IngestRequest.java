package com.demo.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IngestRequest(
		@NotBlank(message = "文档内容不能为空") @Size(max = 100_000, message = "文档内容不能超过 100,000 字符") String content,

		@NotBlank(message = "来源标识不能为空") @Size(max = 255, message = "来源标识不能超过 255 字符") String source) {
}
