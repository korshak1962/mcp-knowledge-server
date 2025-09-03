package com.korshak.mcpserver;

import com.korshak.mcpserver.handler.McpProtocolHandler;
import com.korshak.mcpserver.service.KnowledgeStoreService;
import com.korshak.mcpserver.service.MetadataService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootApplication
public class McpStdinServerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(McpStdinServerApplication.class);
    
    public static void main(String[] args) {
        // Disable web server for stdin version
        System.setProperty("spring.main.web-application-type", "none");
        System.setProperty("logging.level.org.springframework", "WARN");
        System.setProperty("logging.level.com.korshak.mcpserver", "ERROR");
        
        ConfigurableApplicationContext context = SpringApplication.run(McpStdinServerApplication.class, args);
        
        try {
            // Get beans from Spring context
            McpProtocolHandler protocolHandler = context.getBean(McpProtocolHandler.class);
            KnowledgeStoreService knowledgeStoreService = context.getBean(KnowledgeStoreService.class);
            MetadataService metadataService = context.getBean(MetadataService.class);
            
            // Initialize services
            metadataService.loadMetadata();
            
            logger.info("MCP Knowledge Server (stdin) started successfully");
            
            // Start stdin/stdout communication
            StdinHandler stdinHandler = new StdinHandler(protocolHandler);
            stdinHandler.start();
            
        } catch (Exception e) {
            logger.error("Failed to start MCP stdin server", e);
            System.exit(1);
        } finally {
            context.close();
        }
    }
    
    private static class StdinHandler {
        private final McpProtocolHandler protocolHandler;
        private final BufferedReader reader;
        private final ObjectMapper objectMapper;
        private final Logger logger = LoggerFactory.getLogger(StdinHandler.class);
        
        public StdinHandler(McpProtocolHandler protocolHandler) {
            this.protocolHandler = protocolHandler;
            this.reader = new BufferedReader(new InputStreamReader(System.in));
            this.objectMapper = new ObjectMapper();
        }
        
        public void start() {
            logger.info("MCP Server listening on stdin...");
            
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    try {
                        // Process the MCP message
                        String response = protocolHandler.handleMessage(line);
                        
                        // Only write response to stdout if there is one
                        // (notifications don't require responses)
                        if (response != null) {
                            System.out.println(response);
                            System.out.flush();
                        }
                        
                    } catch (Exception e) {
                        logger.error("Error processing message: " + line, e);
                        
                        // Send error response
                        String errorResponse = createErrorResponse(e.getMessage());
                        System.out.println(errorResponse);
                        System.out.flush();
                    }
                }
            } catch (IOException e) {
                logger.error("Error reading from stdin", e);
            }
            
            logger.info("MCP Server stdin handler stopped");
        }
        
        private String createErrorResponse(String message) {
            try {
                Map<String, Object> error = new HashMap<>();
                error.put("code", -32603);
                error.put("message", "Internal error: " + message);
                
                Map<String, Object> response = new HashMap<>();
                response.put("jsonrpc", "2.0");
                response.put("id", "0"); // Use default ID for error responses
                response.put("error", error);
                
                return objectMapper.writeValueAsString(response);
            } catch (Exception e) {
                return "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
            }
        }
    }
}
