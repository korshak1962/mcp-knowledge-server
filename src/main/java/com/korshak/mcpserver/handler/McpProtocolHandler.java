package com.korshak.mcpserver.handler;

import com.korshak.mcpserver.model.*;
import com.korshak.mcpserver.service.KnowledgeStoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class McpProtocolHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(McpProtocolHandler.class);
    
    @Autowired
    private KnowledgeStoreService knowledgeStoreService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String handleMessage(String message) {
        Object requestId = "1"; // Default fallback ID
        
        try {
            JsonNode requestNode = objectMapper.readTree(message);
            
            // Extract ID - it can be string, number, but NEVER null for responses
            if (requestNode.has("id") && !requestNode.get("id").isNull()) {
                JsonNode idNode = requestNode.get("id");
                if (idNode.isTextual()) {
                    requestId = idNode.asText();
                } else if (idNode.isNumber()) {
                    requestId = idNode.asLong();
                } else {
                    requestId = "1"; // Fallback to string
                }
            } else {
                // If no ID provided, use a default string ID
                requestId = "1";
            }
            
            // Extract method
            String method = requestNode.has("method") ? requestNode.get("method").asText() : null;
            if (method == null) {
                return createErrorResponse(requestId, -32600, "Invalid Request: missing method");
            }
            
            // Handle notifications (no response required)
            if (method.equals("notifications/initialized")) {
                logger.info("Received initialization notification");
                return null; // No response for notifications
            }
            
            // Extract params
            JsonNode paramsNode = requestNode.get("params");
            Map<String, Object> params = new HashMap<>();
            if (paramsNode != null && paramsNode.isObject()) {
                params = objectMapper.convertValue(paramsNode, Map.class);
            }
            
            // Process request
            Object result = processRequest(method, params);
            
            // Create success response - NEVER with null ID
            return createSuccessResponse(requestId, result);
            
        } catch (Exception e) {
            logger.error("Error processing MCP message: " + message, e);
            return createErrorResponse(requestId, -32603, "Internal error: " + e.getMessage());
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
            case "prompts/list":
                return handlePromptsList();
            case "resources/list":
                return handleResourcesList();
            default:
                throw new RuntimeException("Method not found: " + method);
        }
    }
    
    private Map<String, Object> handleInitialize(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "Knowledge Store MCP Server");
        serverInfo.put("version", "1.0.0");
        result.put("serverInfo", serverInfo);
        
        Map<String, Object> capabilities = new HashMap<>();
        
        // Indicate that this server supports tools
        Map<String, Object> tools = new HashMap<>();
        capabilities.put("tools", tools);
        
        // Add empty capabilities for prompts and resources (required by MCP spec)
        Map<String, Object> prompts = new HashMap<>();
        capabilities.put("prompts", prompts);
        
        Map<String, Object> resources = new HashMap<>();
        capabilities.put("resources", resources);
        
        result.put("capabilities", capabilities);
        
        return result;
    }
    
    private Map<String, Object> handlePromptsList() {
        // Return empty prompts list - this server doesn't provide prompts
        List<Map<String, Object>> prompts = new ArrayList<>();
        return Map.of("prompts", prompts);
    }
    
    private Map<String, Object> handleResourcesList() {
        // Return empty resources list - this server doesn't provide resources
        List<Map<String, Object>> resources = new ArrayList<>();
        return Map.of("resources", resources);
    }
    
    private Map<String, Object> handleToolsList() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        // Add all tool definitions with proper schema
        tools.add(createTool("list_files", 
            "List all files in the knowledge store with basic info", 
            Map.of("type", "object", "properties", Map.of(), "required", List.of())));
        
        tools.add(createTool("list_files_with_metadata", 
            "List all files with rich metadata including descriptions, tags, categories, and size warnings", 
            Map.of("type", "object", "properties", Map.of(), "required", List.of())));
        
        tools.add(createTool("read_file", 
            "Read the content of a specific file", 
            Map.of("type", "object", 
                "properties", Map.of("filename", Map.of("type", "string", "description", "Name of the file to read")),
                "required", List.of("filename"))));
        
        tools.add(createTool("search_files", 
            "Search for files containing specific text in content", 
            Map.of("type", "object", 
                "properties", Map.of("query", Map.of("type", "string", "description", "Search query")),
                "required", List.of("query"))));
        
        tools.add(createTool("search_files_by_metadata", 
            "Search files by metadata (description, tags, category, summary) - smarter than content search", 
            Map.of("type", "object", 
                "properties", Map.of("query", Map.of("type", "string", "description", "Metadata search query")),
                "required", List.of("query"))));
        
        tools.add(createTool("get_files_by_category", 
            "Get all files in a specific category (document, text, image, etc.)", 
            Map.of("type", "object", 
                "properties", Map.of("category", Map.of("type", "string", "description", "Category name")),
                "required", List.of("category"))));
        
        tools.add(createTool("get_knowledge_store_overview", 
            "Get overview of the knowledge store with statistics and categories", 
            Map.of("type", "object", "properties", Map.of(), "required", List.of())));
        
        tools.add(createTool("get_file_info", 
            "Get detailed metadata information about a file", 
            Map.of("type", "object", 
                "properties", Map.of("filename", Map.of("type", "string", "description", "Name of the file")),
                "required", List.of("filename"))));
        
        tools.add(createTool("update_file_metadata", 
            "Update metadata for a file (description, tags, category, summary)", 
            Map.of("type", "object", 
                "properties", Map.of(
                    "filename", Map.of("type", "string", "description", "Name of the file"),
                    "description", Map.of("type", "string", "description", "File description"),
                    "tags", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Tags for the file"),
                    "category", Map.of("type", "string", "description", "File category"),
                    "summary", Map.of("type", "string", "description", "File summary")
                ),
                "required", List.of("filename"))));
        
        tools.add(createTool("write_file", 
            "Write content to a file", 
            Map.of("type", "object", 
                "properties", Map.of(
                    "filename", Map.of("type", "string", "description", "Name of the file to write"),
                    "content", Map.of("type", "string", "description", "Content to write to the file")
                ),
                "required", List.of("filename", "content"))));
        
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
                case "list_files":
                    List<String> files = knowledgeStoreService.listFiles();
                    return files.isEmpty() ? "No files found in knowledge store" : 
                           "Files in knowledge store:\n" + String.join("\n", files);
                    
                case "list_files_with_metadata":
                    List<FileMetadata> filesWithMetadata = knowledgeStoreService.listFilesWithMetadata();
                    if (filesWithMetadata.isEmpty()) {
                        return "No files found in knowledge store";
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("📚 Files in Knowledge Store with Metadata:\n");
                        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                        
                        for (FileMetadata metadata : filesWithMetadata) {
                            sb.append("\n📄 ").append(metadata.getFilename());
                            
                            if (metadata.getDescription() != null) {
                                sb.append("\n   📝 Description: ").append(metadata.getDescription());
                            }
                            
                            if (metadata.getSummary() != null) {
                                sb.append("\n   📋 Summary: ").append(metadata.getSummary());
                            }
                            
                            sb.append("\n   📂 Category: ").append(metadata.getCategory());
                            
                            if (metadata.getTags() != null && !metadata.getTags().isEmpty()) {
                                sb.append("\n   🏷️  Tags: ").append(String.join(", ", metadata.getTags()));
                            }
                            
                            sb.append("\n   💾 Size: ").append(formatFileSize(metadata.getSize()));
                            
                            if (metadata.isLargeFile()) {
                                sb.append(" ⚠️ Large file - consider reading summary first");
                            }
                            
                            sb.append("\n");
                        }
                        return sb.toString();
                    }
                    
                case "read_file":
                    String filename = (String) arguments.get("filename");
                    if (filename == null) {
                        return "Error: filename parameter is required";
                    }
                    return knowledgeStoreService.readFile(filename);
                    
                case "search_files":
                    String query = (String) arguments.get("query");
                    if (query == null) {
                        return "Error: query parameter is required";
                    }
                    return knowledgeStoreService.searchFiles(query);
                    
                case "search_files_by_metadata":
                    String metadataQuery = (String) arguments.get("query");
                    if (metadataQuery == null) {
                        return "Error: query parameter is required";
                    }
                    return knowledgeStoreService.searchFilesByMetadata(metadataQuery);
                    
                case "get_files_by_category":
                    String category = (String) arguments.get("category");
                    if (category == null) {
                        return "Error: category parameter is required";
                    }
                    return knowledgeStoreService.getFilesByCategory(category);
                    
                case "get_knowledge_store_overview":
                    return knowledgeStoreService.getKnowledgeStoreOverview();
                    
                case "get_file_info":
                    String infoFilename = (String) arguments.get("filename");
                    if (infoFilename == null) {
                        return "Error: filename parameter is required";
                    }
                    Map<String, Object> fileInfo = knowledgeStoreService.getFileInfo(infoFilename);
                    return objectMapper.writeValueAsString(fileInfo);
                    
                case "update_file_metadata":
                    String metaFilename = (String) arguments.get("filename");
                    if (metaFilename == null) {
                        return "Error: filename parameter is required";
                    }
                    String description = (String) arguments.get("description");
                    @SuppressWarnings("unchecked")
                    List<String> tags = (List<String>) arguments.get("tags");
                    String metaCategory = (String) arguments.get("category");
                    String summary = (String) arguments.get("summary");
                    
                    return knowledgeStoreService.updateFileMetadata(
                        metaFilename, description, tags, metaCategory, summary);
                    
                case "write_file":
                    String writeFilename = (String) arguments.get("filename");
                    String content = (String) arguments.get("content");
                    if (writeFilename == null || content == null) {
                        return "Error: filename and content parameters are required";
                    }
                    return knowledgeStoreService.writeFile(writeFilename, content);
                    
                default:
                    return "Unknown tool: " + toolName;
            }
            
        } catch (Exception e) {
            logger.error("Error executing tool call: " + toolName, e);
            return "Error: " + e.getMessage();
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    private String createSuccessResponse(Object id, Object result) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            
            // CRITICAL: Ensure ID is never null - use string fallback
            if (id == null) {
                response.put("id", "0");
            } else {
                response.put("id", id);
            }
            
            response.put("result", result);
            
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Error creating success response", e);
            return createErrorResponse(id, -32603, "Error serializing response");
        }
    }
    
    private String createErrorResponse(Object id, int code, String message) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("code", code);
            error.put("message", message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jsonrpc", "2.0");
            
            // CRITICAL: Ensure ID is never null - use string fallback
            if (id == null) {
                response.put("id", "0");
            } else {
                response.put("id", id);
            }
            
            response.put("error", error);
            
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Error creating error response", e);
            return "{\"jsonrpc\":\"2.0\",\"id\":\"0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
        }
    }
}
