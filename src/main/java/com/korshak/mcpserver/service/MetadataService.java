package com.korshak.mcpserver.service;

import com.korshak.mcpserver.model.FileMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetadataService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);
    private static final String METADATA_FILE = "file-metadata.json";
    private static final int LARGE_FILE_THRESHOLD = 50000; // characters
    
    @Value("${knowledge.store.path:./knowledgeStore}")
    private String knowledgeStorePath;
    
    private final ObjectMapper objectMapper;
    private final Map<String, FileMetadata> metadataCache = new ConcurrentHashMap<>();
    
    public MetadataService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public void loadMetadata() {
        try {
            Path metadataPath = Paths.get(knowledgeStorePath, METADATA_FILE);
            if (Files.exists(metadataPath)) {
                String jsonContent = Files.readString(metadataPath);
                Map<String, FileMetadata> metadata = objectMapper.readValue(
                    jsonContent, new TypeReference<Map<String, FileMetadata>>() {}
                );
                metadataCache.putAll(metadata);
                logger.info("Loaded metadata for {} files", metadata.size());
            }
        } catch (IOException e) {
            logger.error("Error loading metadata", e);
        }
    }
    
    public void saveMetadata() {
        try {
            Path storePath = Paths.get(knowledgeStorePath);
            if (!Files.exists(storePath)) {
                Files.createDirectories(storePath);
            }
            
            Path metadataPath = Paths.get(knowledgeStorePath, METADATA_FILE);
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(metadataCache);
            Files.writeString(metadataPath, jsonContent);
            logger.debug("Saved metadata for {} files", metadataCache.size());
        } catch (IOException e) {
            logger.error("Error saving metadata", e);
        }
    }
    
    public FileMetadata getOrCreateMetadata(String filename) {
        FileMetadata metadata = metadataCache.get(filename);
        if (metadata == null) {
            metadata = createDefaultMetadata(filename);
            metadataCache.put(filename, metadata);
        }
        return metadata;
    }
    
    private FileMetadata createDefaultMetadata(String filename) {
        FileMetadata metadata = new FileMetadata(filename);
        
        try {
            Path filePath = Paths.get(knowledgeStorePath, filename);
            if (Files.exists(filePath)) {
                metadata.setSize(Files.size(filePath));
                metadata.setLastModified(
                    LocalDateTime.ofInstant(
                        Files.getLastModifiedTime(filePath).toInstant(),
                        ZoneId.systemDefault()
                    )
                );
                
                // Auto-detect category based on extension
                String extension = getFileExtension(filename).toLowerCase();
                metadata.setCategory(categorizeByExtension(extension));
                
                // Estimate if it's a large file
                metadata.setLargeFile(metadata.getSize() > LARGE_FILE_THRESHOLD);
                
                // Set basic tags
                metadata.setTags(generateDefaultTags(filename, extension));
            }
        } catch (IOException e) {
            logger.error("Error creating metadata for file: " + filename, e);
        }
        
        return metadata;
    }
    
    private String categorizeByExtension(String extension) {
        switch (extension) {
            case "pdf": return "document";
            case "txt":
            case "md": return "text";
            case "rtf": return "document";
            case "docx":
            case "doc": return "document";
            case "xlsx":
            case "xls": return "spreadsheet";
            case "pptx":
            case "ppt": return "presentation";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif": return "image";
            case "mp4":
            case "avi":
            case "mov": return "video";
            case "mp3":
            case "wav": return "audio";
            case "zip":
            case "rar": return "archive";
            default: return "other";
        }
    }
    
    private List<String> generateDefaultTags(String filename, String extension) {
        List<String> tags = new ArrayList<>();
        tags.add(extension);
        
        // Add tags based on filename patterns
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.contains("report")) tags.add("report");
        if (lowerFilename.contains("spec") || lowerFilename.contains("requirement")) tags.add("specification");
        if (lowerFilename.contains("manual") || lowerFilename.contains("guide")) tags.add("guide");
        if (lowerFilename.contains("contract") || lowerFilename.contains("agreement")) tags.add("legal");
        if (lowerFilename.contains("budget") || lowerFilename.contains("financial")) tags.add("finance");
        if (lowerFilename.contains("meeting") || lowerFilename.contains("notes")) tags.add("meeting");
        
        return tags;
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
    
    public void updateMetadata(String filename, FileMetadata updatedMetadata) {
        metadataCache.put(filename, updatedMetadata);
        saveMetadata();
    }
    
    public void updateFileAccess(String filename) {
        FileMetadata metadata = getOrCreateMetadata(filename);
        metadata.setLastAccessed(LocalDateTime.now());
        saveMetadata();
    }
    
    public List<FileMetadata> searchByMetadata(String query) {
        List<FileMetadata> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (FileMetadata metadata : metadataCache.values()) {
            if (matchesQuery(metadata, lowerQuery)) {
                results.add(metadata);
            }
        }
        
        return results;
    }
    
    private boolean matchesQuery(FileMetadata metadata, String query) {
        // Check filename
        if (metadata.getFilename() != null && 
            metadata.getFilename().toLowerCase().contains(query)) {
            return true;
        }
        
        // Check description
        if (metadata.getDescription() != null && 
            metadata.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        
        // Check tags
        if (metadata.getTags() != null) {
            for (String tag : metadata.getTags()) {
                if (tag.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }
        
        // Check category
        if (metadata.getCategory() != null && 
            metadata.getCategory().toLowerCase().contains(query)) {
            return true;
        }
        
        // Check summary
        if (metadata.getSummary() != null && 
            metadata.getSummary().toLowerCase().contains(query)) {
            return true;
        }
        
        return false;
    }
    
    public List<FileMetadata> getFilesByCategory(String category) {
        return metadataCache.values().stream()
            .filter(metadata -> category.equals(metadata.getCategory()))
            .sorted(Comparator.comparing(FileMetadata::getLastModified, 
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }
    
    public List<FileMetadata> getRecentFiles(int limit) {
        return metadataCache.values().stream()
            .sorted(Comparator.comparing(FileMetadata::getLastAccessed, 
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(limit)
            .toList();
    }
    
    public Map<String, Object> getKnowledgeStoreOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        Map<String, Long> categoryCounts = new HashMap<>();
        int totalFiles = 0;
        long totalSize = 0;
        int largeFiles = 0;
        
        for (FileMetadata metadata : metadataCache.values()) {
            totalFiles++;
            totalSize += metadata.getSize();
            
            if (metadata.isLargeFile()) {
                largeFiles++;
            }
            
            String category = metadata.getCategory();
            if (category != null) {
                categoryCounts.merge(category, 1L, Long::sum);
            }
        }
        
        overview.put("totalFiles", totalFiles);
        overview.put("totalSize", totalSize);
        overview.put("largeFiles", largeFiles);
        overview.put("categoryCounts", categoryCounts);
        overview.put("lastUpdated", LocalDateTime.now());
        
        return overview;
    }
}
