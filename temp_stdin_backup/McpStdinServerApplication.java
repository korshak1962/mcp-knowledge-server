package com.korshak.mcpserver.stdin;

import com.korshak.mcpserver.handler.McpProtocolHandler;
import com.korshak.mcpserver.service.KnowledgeStoreService;
import com.korshak.mcpserver.service.MetadataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(scanBasePackages = "com.korshak.mcpserver")
public class McpStdinServerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(McpStdinServerApplication.class);
    
    public static void main(String[] args) {
        // CRITICAL: Suppress ALL Spring Boot output to stdout
        System.setProperty("spring.main.web-application-type", "none");
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("spring.output.ansi.enabled", "never");
        System.setProperty("logging.level.root", "OFF");
        System.setProperty("logging.level.org.springframework", "OFF");
        System.setProperty("logging.level.com.korshak.mcpserver", "OFF");
        
        // Redirect Spring Boot logs to stderr to keep stdout clean for MCP
        System.setProperty("logging.pattern.console", "");
        
        try {
            ConfigurableApplicationContext context = SpringApplication.run(McpStdinServerApplication.class, args);
            
            // Get beans from Spring context
            McpProtocolHandler protocolHandler = context.getBean(McpProtocolHandler.class);
            KnowledgeStoreService knowledgeStoreService = context.getBean(KnowledgeStoreService.class);
            MetadataService metadataService = context.getBean(MetadataService.class);
            
            // Initialize services
            metadataService.loadMetadata();
            
            // Start stdin/stdout communication
            StdinHandler stdinHandler = new StdinHandler(protocolHandler);
            stdinHandler.start();
            
        } catch (Exception e) {
            // Log to stderr only, never to stdout
            System.err.println("Failed to start MCP stdin server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static class StdinHandler {
        private final McpProtocolHandler protocolHandler;
        private final BufferedReader reader;
        private final ObjectMapper objectMapper;
        
        public StdinHandler(McpProtocolHandler protocolHandler) {
            this.protocolHandler = protocolHandler;
            this.reader = new BufferedReader(new InputStreamReader(System.in));
            this.objectMapper = new ObjectMapper();
        }
        
        public void start() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    try {
                        // Process the MCP message and get response
                        String response = protocolHandler.handleMessage(line);
                        
                        // Validate response before sending
                        if (isValidMcpResponse(response)) {
                            // Write response to stdout (ONLY valid JSON)
                            System.out.println(response);
                            System.out.flush();
                        } else {
                            // Create a proper error response with guaranteed non-null ID
                            String errorResponse = createValidErrorResponse("0", -32603, "Invalid response format");
                            System.out.println(errorResponse);
                            System.out.flush();
                        }
                        
                    } catch (Exception e) {
                        // Send a properly formatted error response with guaranteed non-null ID
                        String errorResponse = createValidErrorResponse("0", -32603, "Server error: " + e.getMessage());
                        System.out.println(errorResponse);
                        System.out.flush();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from stdin: " + e.getMessage());
            }
        }
        
        private boolean isValidMcpResponse(String response) {
            try {
                JsonNode node = objectMapper.readTree(response);
                
                // Must have jsonrpc field
                if (!node.has("jsonrpc") || !"2.0".equals(node.get("jsonrpc").asText())) {
                    return false;
                }
                
                // Must have ID field and it CANNOT be null
                if (!node.has("id") || node.get("id").isNull()) {
                    return false;
                }
                
                // Must have either result OR error, but not both
                boolean hasResult = node.has("result");
                boolean hasError = node.has("error");
                
                if (hasResult && hasError) return false;
                if (!hasResult && !hasError) return false;
                
                // If it has an error, it must have proper error structure
                if (hasError) {
                    JsonNode error = node.get("error");
                    if (!error.has("code") || !error.has("message")) {
                        return false;
                    }
                }
                
                return true;
                
            } catch (Exception e) {
                return false;
            }
        }
        
        private String createValidErrorResponse(String id, int code, String message) {
            try {
                Map<String, Object> error = new HashMap<>();
                error.put("code", code);
                error.put("message", message);
                
                Map<String, Object> response = new HashMap<>();
                response.put("jsonrpc", "2.0");
                response.put("id", id); // Always provide a valid string ID
                response.put("error", error);
                
                return objectMapper.writeValueAsString(response);
            } catch (Exception e) {
                return "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
            }
        }
    }
}
