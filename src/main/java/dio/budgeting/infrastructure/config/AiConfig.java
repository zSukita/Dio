package dio.budgeting.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dio.budgeting.application.ai.TransactionTools;

@Configuration
public class AiConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(TransactionTools transactionTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(transactionTools)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {
        return builder
                .defaultTools(toolCallbackProvider)
                .build();
    }
}