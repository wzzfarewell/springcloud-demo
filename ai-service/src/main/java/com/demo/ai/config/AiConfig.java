package com.demo.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class AiConfig {

    // RAG 模式的 system prompt，指导 LLM 严格基于上下文回答
    private static final String RAG_SYSTEM_PROMPT = """
            你是一个专业的知识库助手。请严格根据提供的上下文信息来回答问题，\
            不要编造不存在于上下文中的内容。如果上下文中没有相关信息，\
            请明确告知用户"知识库中暂无相关信息"。回答请使用中文，简洁清晰。
            """;

    /**
     * 内存向量库（SimpleVectorStore）
     * 生产环境可替换为：PgVectorStore / ChromaVectorStore / RedisVectorStore 等
     */
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * Token 文本切分器：chunk 大小与重叠窗口由 app.rag.* 配置驱动，便于运行时调优
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter(RagProperties props) {
        return new TokenTextSplitter(props.chunkSize(), props.chunkOverlap(), 5, 10000, true);
    }

    /** RAG ChatClient：携带知识库助手 system prompt */
    @Bean("ragChatClient")
    public ChatClient ragChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(RAG_SYSTEM_PROMPT).build();
    }

    /** 普通 ChatClient：直接与 LLM 对话，用于效果对比 */
    @Bean("simpleChatClient")
    public ChatClient simpleChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
