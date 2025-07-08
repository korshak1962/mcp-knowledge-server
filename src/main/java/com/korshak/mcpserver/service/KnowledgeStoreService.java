package com.korshak.mcpserver.service;

import com.korshak.mcpserver.model.FileMetadata;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class KnowledgeStoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeStoreService.class);
    
    @Value("${knowledge.store.path:./knowledgeStore}")
    private String knowledgeStorePath;
    
    @Autowired
    private MetadataService metadataService;
    
    private final Tika tika = new Tika();
    
    @PostConstruct
    public void init() {
        metadataService.loadMetadata();
    }
    
    public List<String> listFiles() {
        List<String> files = new ArrayList<>();
        try {
            Path storePath = Paths.get(knowledgeStorePath);
            if (!Files.exists(storePath)) {
                Files.createDirectories(storePath);
                return files;
            }
            
            try (Stream<Path> paths = Files.walk(storePath)) {
                paths.filter(Files::isRegularFile)
                     .forEach(path -> files.add(path.toString()));
            }
        } catch (IOException e) {
            logger.error("Error listing files in knowledge store", e);
        }
        return files;
    }
    
    public String readFile(String filename) {
        try {
            Path filePath = Paths.get(knowledgeStorePath, filename);
            if (!Files.exists(filePath)) {
                return "File not found: " + filename;
            }
            
            // Update access time in metadata
            metadataService.updateFileAccess(filename);
            
            String extension = FilenameUtils.getExtension(filename).toLowerCase();
            
            switch (extension) {
                case "pdf":
                    return readPdfFile(filePath);
                case "txt":
                case "md":
                    return Files.readString(filePath);
                case "rtf":
                    return readWithTika(filePath); // RTF handled by Tika
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                    return readImageFile(filePath);
                default:
                    return readWithTika(filePath);
            }
        } catch (Exception e) {
            logger.error("Error reading file: " + filename, e);
            return "Error reading file: " + e.getMessage();
        }
    }
    
    private String readPdfFile(Path filePath) throws IOException {
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private String readImageFile(Path filePath) {
        try {
            // For images, we'll return metadata and a description
            String mimeType = tika.detect(filePath.toFile());
            long size = Files.size(filePath);
            
            return String.format("Image file: %s%nMIME Type: %s%nFile Size: %d bytes%n" +
                    "Note: This is an image file. Text extraction from images requires OCR capability.",
                    filePath.getFileName(), mimeType, size);
        } catch (IOException e) {
            logger.error("Error reading image file", e);
            return "Error reading image file: " + e.getMessage();
        }
    }
    
    private String readWithTika(Path filePath) {
        try {
            return tika.parseToString(filePath.toFile());
        } catch (IOException | TikaException e) {
            logger.error("Error reading file with Tika", e);
            return "Error reading file with Tika: " + e.getMessage();
        }
    }
    
    public String searchFiles(String query) {
        List<String> results = new ArrayList<>();
        List<String> files = listFiles();
        
        for (String file : files) {
            String content = readFile(Paths.get(file).getFileName().toString());
            if (content.toLowerCase().contains(query.toLowerCase())) {
                results.add(file + " - Content matches query");
            }
        }
        
        return results.isEmpty() ? "No files found matching query: " + query : 
               "Files matching query '" + query + "':\n" + String.join("\n", results);
    }
    
    public Map<String, Object> getFileInfo(String filename) {
        Map<String, Object> info = new HashMap<>();
        try {
            Path filePath = Paths.get(knowledgeStorePath, filename);
            if (!Files.exists(filePath)) {
                info.put("error", "File not found: " + filename);
                return info;
            }
            
            info.put("filename", filename);
            info.put("size", Files.size(filePath));
            info.put("lastModified", Files.getLastModifiedTime(filePath).toString());
            info.put("mimeType", tika.detect(filePath.toFile()));
            info.put("extension", FilenameUtils.getExtension(filename));
            
        } catch (IOException e) {
            logger.error("Error getting file info", e);
            info.put("error", "Error getting file info: " + e.getMessage());
        }
        return info;
    }
    
    public String writeFile(String filename, String content) {
        try {
            Path filePath = Paths.get(knowledgeStorePath, filename);
            Path parentDir = filePath.getParent();
            
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            Files.writeString(filePath, content);
            
            // Update or create metadata
            FileMetadata metadata = metadataService.getOrCreateMetadata(filename);
            metadata.setSize(content.length());
            metadata.setEstimatedTokens(estimateTokens(content));
            metadata.setLargeFile(content.length() > 50000);
            metadataService.updateMetadata(filename, metadata);
            
            return "File written successfully: " + filename;
        } catch (IOException e) {
            logger.error("Error writing file", e);
            return "Error writing file: " + e.getMessage();
        }
    }
    
    // New metadata-enhanced methods
    
    public List<FileMetadata> listFilesWithMetadata() {
        List<String> filenames = listFiles();
        List<FileMetadata> filesWithMetadata = new ArrayList<>();
        
        for (String filename : filenames) {
            String shortName = Paths.get(filename).getFileName().toString();
            FileMetadata metadata = metadataService.getOrCreateMetadata(shortName);
            filesWithMetadata.add(metadata);
        }
        
        return filesWithMetadata;
    }
    
    public String updateFileMetadata(String filename, String description, 
                                   List<String> tags, String category, String summary) {
        try {
            FileMetadata metadata = metadataService.getOrCreateMetadata(filename);
            
            if (description != null) metadata.setDescription(description);
            if (tags != null) metadata.setTags(tags);
            if (category != null) metadata.setCategory(category);
            if (summary != null) metadata.setSummary(summary);
            
            metadataService.updateMetadata(filename, metadata);
            
            return "Metadata updated successfully for: " + filename;
        } catch (Exception e) {
            logger.error("Error updating metadata", e);
            return "Error updating metadata: " + e.getMessage();
        }
    }
    
    public String searchFilesByMetadata(String query) {
        List<FileMetadata> results = metadataService.searchByMetadata(query);
        
        if (results.isEmpty()) {
            return "No files found matching metadata query: " + query;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Files matching metadata query '").append(query).append("':\n");
        
        for (FileMetadata metadata : results) {
            sb.append("\n📄 ").append(metadata.getFilename());
            
            if (metadata.getDescription() != null) {
                sb.append("\n   Description: ").append(metadata.getDescription());
            }
            
            if (metadata.getSummary() != null) {
                sb.append("\n   Summary: ").append(metadata.getSummary());
            }
            
            if (metadata.getTags() != null && !metadata.getTags().isEmpty()) {
                sb.append("\n   Tags: ").append(String.join(", ", metadata.getTags()));
            }
            
            sb.append("\n   Category: ").append(metadata.getCategory());
            sb.append("\n   Size: ").append(formatFileSize(metadata.getSize()));
            
            if (metadata.isLargeFile()) {
                sb.append(" ⚠️ Large file - consider using summary");
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public String getFilesByCategory(String category) {
        List<FileMetadata> files = metadataService.getFilesByCategory(category);
        
        if (files.isEmpty()) {
            return "No files found in category: " + category;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Files in category '").append(category).append("':\n");
        
        for (FileMetadata metadata : files) {
            sb.append("\n📄 ").append(metadata.getFilename());
            if (metadata.getDescription() != null) {
                sb.append(" - ").append(metadata.getDescription());
            }
            if (metadata.isLargeFile()) {
                sb.append(" ⚠️ Large file");
            }
        }
        
        return sb.toString();
    }
    
    public String getKnowledgeStoreOverview() {
        Map<String, Object> overview = metadataService.getKnowledgeStoreOverview();
        
        StringBuilder sb = new StringBuilder();
        sb.append("📚 Knowledge Store Overview\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📊 Total Files: ").append(overview.get("totalFiles")).append("\n");
        sb.append("💾 Total Size: ").append(formatFileSize((Long) overview.get("totalSize"))).append("\n");
        sb.append("⚠️  Large Files: ").append(overview.get("largeFiles")).append("\n\n");
        
        @SuppressWarnings("unchecked")
        Map<String, Long> categoryCounts = (Map<String, Long>) overview.get("categoryCounts");
        
        if (!categoryCounts.isEmpty()) {
            sb.append("📂 Files by Category:\n");
            categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> 
                    sb.append("   ").append(entry.getKey())
                      .append(": ").append(entry.getValue()).append("\n")
                );
        }
        
        return sb.toString();
    }
    
    private int estimateTokens(String content) {
        // Rough estimation: ~4 characters per token
        return content.length() / 4;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
