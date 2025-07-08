package com.korshak.mcpserver.controller;

import com.korshak.mcpserver.model.FileMetadata;
import com.korshak.mcpserver.service.KnowledgeStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeStoreController {
    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeStoreController.class);
    
    @Autowired
    private KnowledgeStoreService knowledgeStoreService;
    
    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = knowledgeStoreService.listFiles();
        return ResponseEntity.ok(files);
    }
    
    @GetMapping("/files/{filename}")
    public ResponseEntity<String> readFile(@PathVariable String filename) {
        String content = knowledgeStoreService.readFile(filename);
        return ResponseEntity.ok(content);
    }
    
    @GetMapping("/files/{filename}/info")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String filename) {
        Map<String, Object> info = knowledgeStoreService.getFileInfo(filename);
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/search")
    public ResponseEntity<String> searchFiles(@RequestParam String query) {
        String results = knowledgeStoreService.searchFiles(query);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/files/{filename}")
    public ResponseEntity<String> writeFile(@PathVariable String filename, @RequestBody String content) {
        String result = knowledgeStoreService.writeFile(filename, content);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            String filename = file.getOriginalFilename();
            Path filePath = Paths.get("./knowledgeStore", filename);
            
            // Create directories if they don't exist
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Save the file
            file.transferTo(filePath.toFile());
            
            logger.info("File uploaded successfully: {}", filename);
            return ResponseEntity.ok("File uploaded successfully: " + filename);
            
        } catch (IOException e) {
            logger.error("Error uploading file", e);
            return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "MCP Knowledge Server",
            "version", "1.0.0"
        ));
    }
    
    // New metadata endpoints
    
    @GetMapping("/files-with-metadata")
    public ResponseEntity<List<FileMetadata>> listFilesWithMetadata() {
        List<FileMetadata> files = knowledgeStoreService.listFilesWithMetadata();
        return ResponseEntity.ok(files);
    }
    
    @GetMapping("/overview")
    public ResponseEntity<String> getOverview() {
        String overview = knowledgeStoreService.getKnowledgeStoreOverview();
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/search-metadata")
    public ResponseEntity<String> searchByMetadata(@RequestParam String query) {
        String results = knowledgeStoreService.searchFilesByMetadata(query);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<String> getFilesByCategory(@PathVariable String category) {
        String results = knowledgeStoreService.getFilesByCategory(category);
        return ResponseEntity.ok(results);
    }
    
    @PutMapping("/files/{filename}/metadata")
    public ResponseEntity<String> updateFileMetadata(
            @PathVariable String filename,
            @RequestBody UpdateMetadataRequest request) {
        String result = knowledgeStoreService.updateFileMetadata(
            filename, 
            request.getDescription(), 
            request.getTags(), 
            request.getCategory(), 
            request.getSummary()
        );
        return ResponseEntity.ok(result);
    }
    
    // Request DTO for metadata updates
    public static class UpdateMetadataRequest {
        private String description;
        private List<String> tags;
        private String category;
        private String summary;
        
        // Getters and setters
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<String> getTags() {
            return tags;
        }
        
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getSummary() {
            return summary;
        }
        
        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}
