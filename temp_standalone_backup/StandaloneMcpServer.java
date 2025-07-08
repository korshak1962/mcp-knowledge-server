package com.korshak.mcpserver.standalone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Standalone MCP Knowledge Server - Direct JSON-RPC implementation
 * No Spring Boot dependencies to avoid initialization issues
 */
public class StandaloneMcpServer {
    
    private static final String KNOWLEDGE_STORE_PATH = "D:/mcp-knowledge-server/knowledgeStore";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        // Suppress all logging output to stdout
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");
        
        StandaloneMcpServer server = new StandaloneMcpServer();
        server.start();
    }
    
    public void start() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String response = handleMessage(line);
                    System.out.println(response);
                    System.out.flush();
                } catch (Exception e) {
                    String errorResponse = createErrorResponse("1", -32603, "Server error: " + e.getMessage());
                    System.out.println(errorResponse);
                    System.out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from stdin: " + e.getMessage());
        }
    }
    
    public String handleMessage(String message) {
        try {
            JsonNode requestNode = objectMapper.readTree(message);
            
            // Extract ID - ensure it's never null in response
            String requestId = "1"; // Default fallback
            if (requestNode.has("id") && !requestNode.get("id").isNull()) {
                JsonNode idNode = requestNode.get("id");
                if (idNode.isTextual()) {
                    requestId = idNode.asText();
                } else if (idNode.isNumber()) {
                    requestId = String.valueOf(idNode.asLong());
                }
            }
            
            // Extract method
            String method = requestNode.has("method") ? requestNode.get("method").asText() : null;
            if (method == null) {
                return createErrorResponse(requestId, -32600, "Invalid Request: missing method");
            }
            
            // Extract params
            JsonNode paramsNode = requestNode.get("params");
            Map<String, Object> params = new HashMap<>();
            if (paramsNode != null && paramsNode.isObject()) {
                params = objectMapper.convertValue(paramsNode, Map.class);
            }
            
            // Process request
            Object result = processRequest(method, params);
            
            // Create success response
            return createSuccessResponse(requestId, result);
            
        } catch (Exception e) {
            return createErrorResponse("1", -32603, "Internal error: " + e.getMessage());
        }
    }
    
    private Object processRequest(String method, Map<String, Object> params) throws Exception {
        switch (method) {
            case "initialize":
                return handleInitialize(params);
            case "tools/list":
                return handleToolsList();
            case "tools/call":
                return handleToolsCall(params);
            default:
                throw new RuntimeException("Method not found: " + method);
        }
    }
    
    private Map<String, Object> handleInitialize(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "Standalone Knowledge Store MCP Server");
        serverInfo.put("version", "1.0.0");
        result.put("serverInfo", serverInfo);
        
        Map<String, Object> capabilities = new HashMap<>();
        Map<String, Object> tools = new HashMap<>();
        capabilities.put("tools", tools);
        result.put("capabilities", capabilities);
        
        return result;
    }
    
    private Map<String, Object> handleToolsList() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        tools.add(createTool("read_file", 
            "Read the content of a specific file", 
            Map.of("type", "object", 
                "properties", Map.of("filename", Map.of("type", "string", "description", "Name of the file to read")),
                "required", List.of("filename"))));
        
        tools.add(createTool("list_files", 
            "List all files in the knowledge store", 
            Map.of("type", "object", "properties", Map.of(), "required", List.of())));
        
        tools.add(createTool("search_files", 
            "Search for files containing specific text", 
            Map.of("type", "object", 
                "properties", Map.of("query", Map.of("type", "string", "description", "Search query")),
                "required", List.of("query"))));
        
        return Map.of("tools", tools);
    }
    
    private Map<String, Object> createTool(String name, String description, Object inputSchema) {
        Map<String, Object> tool = new HashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        tool.put("inputSchema", inputSchema);
        return tool;
    }
    
    private Map<String, Object> handleToolsCall(Map<String, Object> params) throws Exception {
        if (params == null) {
            throw new IllegalArgumentException("Missing params for tools/call");
        }
        
        String toolName = (String) params.get("name");
        if (toolName == null) {
            throw new IllegalArgumentException("Missing tool name");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        if (arguments == null) {
            arguments = new HashMap<>();
        }
        
        String resultText = executeToolCall(toolName, arguments);
        
        // Create proper content structure
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", resultText);
        content.add(textContent);
        
        return Map.of("content", content);
    }
    
    private String executeToolCall(String toolName, Map<String, Object> arguments) {
        try {
            switch (toolName) {
                case "read_file":
                    String filename = (String) arguments.get("filename");
                    if (filename == null) {
                        return "Error: filename parameter is required";
                    }
                    return readFile(filename);
                    
                case "list_files":
                    return listFiles();
                    
                case "search_files":
                    String query = (String) arguments.get("query");
                    if (query == null) {
                        return "Error: query parameter is required";
                    }
                    return searchFiles(query);
                    
                default:
                    return "Unknown tool: " + toolName;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String readFile(String filename) {
        try {
            Path filePath = Paths.get(KNOWLEDGE_STORE_PATH, filename);
            if (!Files.exists(filePath)) {
                return "File not found: " + filename;
            }
            
            String extension = FilenameUtils.getExtension(filename).toLowerCase();
            
            if ("txt".equals(extension) || "md".equals(extension)) {
                return Files.readString(filePath);
            } else {
                return "File type not supported for reading: " + extension;
            }
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }
    
    private String listFiles() {
        try {
            Path storePath = Paths.get(KNOWLEDGE_STORE_PATH);
            if (!Files.exists(storePath)) {
                return "Knowledge store directory not found";
            }
            
            List<String> files = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(storePath)) {
                paths.filter(Files::isRegularFile)
                     .forEach(path -> files.add(path.getFileName().toString()));
            }
            
            if (files.isEmpty()) {
                return "No files found in knowledge store";
            }
            
            return "Files in knowledge store:\n" + String.join("\n", files);
        } catch (Exception e) {
            return "Error listing files: " + e.getMessage();
        }
    }
    
    private String searchFiles(String query) {
        try {
            Path storePath = Paths.get(KNOWLEDGE_STORE_PATH);
            if (!Files.exists(storePath)) {
                return "Knowledge store directory not found";
            }
            
            List<String> results = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(storePath)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> {
                         String filename = path.getFileName().toString().toLowerCase();
                         return filename.endsWith(".txt") || filename.endsWith(".md");
                     })
                     .forEach(path -> {
                         try {
                             String content = Files.readString(path);
                             if (content.toLowerCase().contains(query.toLowerCase())) {
                                 results.add(path.getFileName().toString() + " - Content matches query");
                             }
                         } catch (IOException e) {
                             // Skip files that can't be read
                         }
                     });
            }
            
            if (results.isEmpty()) {
                return "No files found matching query: " + query;
            }
            
            return "Files matching query '" + query + "':\n" + String.join("\n", results);
        } catch (Exception e) {
            return "Error searching files: " + e.getMessage();
        }
    }
    
    private String createSuccessResponse(String id, Object result) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id); // Always string, never null
            response.put("result", result);
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return createErrorResponse(id, -32603, "Error serializing response");
        }
    }
    
    private String createErrorResponse(String id, int code, String message) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("code", code);
            error.put("message", message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id); // Always string, never null
            response.put("error", error);
            
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"jsonrpc\":\"2.0\",\"id\":\"" + id + "\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
        }
    }
}
