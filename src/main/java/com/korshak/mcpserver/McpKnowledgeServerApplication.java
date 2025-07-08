package com.korshak.mcpserver;

import com.korshak.mcpserver.service.KnowledgeStoreService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@SpringBootApplication
@EnableWebSocket
public class McpKnowledgeServerApplication implements WebSocketConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(McpKnowledgeServerApplication.class, args);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new McpWebSocketHandler(), "/mcp").setAllowedOrigins("*");
    }

    @Bean
    public KnowledgeStoreService knowledgeStoreService() {
        return new KnowledgeStoreService();
    }
}
