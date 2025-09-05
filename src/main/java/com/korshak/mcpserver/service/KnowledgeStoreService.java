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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
    
    /**
     * Extract all definitions from all files in the knowledge store.
     * Analyzes text content to find definition patterns and returns them as a map.
     * @return Map where key is the term and value is the definition
     */
    public Map<String, String> getAllDefinitions() {
        Map<String, String> definitions = new HashMap<>();
        List<String> files = listFiles();
        
        for (String file : files) {
            String filename = Paths.get(file).getFileName().toString();
            String content = readFile(filename);
            
            if (!content.startsWith("Error") && !content.startsWith("File not found")) {
                Map<String, String> fileDefinitions = extractDefinitionsFromText(content, filename);
                definitions.putAll(fileDefinitions);
            }
        }
        
        return definitions;
    }
    
    /**
     * Extract definitions from a single text using various definition patterns.
     * @param text The text to analyze
     * @param filename The source filename for context
     * @return Map of term -> definition pairs found in the text
     */
    private Map<String, String> extractDefinitionsFromText(String text, String filename) {
        Map<String, String> definitions = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return definitions;
        }
        
        String[] lines = text.split("\n");
        
        // Pattern 1: "Term: Definition" or "Term - Definition"
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Match patterns like "Term: definition" or "Term - definition"
            if (line.matches("^[А-ЯЁA-Z][\\w\\s()\\-]{2,50}[:\\-]\\s+.{10,}$")) {
                String[] parts = line.split("[:\\-]", 2);
                if (parts.length == 2) {
                    String term = parts[0].trim();
                    String definition = parts[1].trim();
                    if (isValidDefinition(term, definition)) {
                        definitions.put(term, definition + " [Source: " + filename + "]");
                    }
                }
            }
            
            // Pattern 2: "Term (определение) - definition" for Russian texts
            if (line.matches("^[А-ЯЁA-Z].+\\s*\\([^)]+\\)\\s*[\\-–—]\\s*.{10,}$")) {
                int openParen = line.indexOf('(');
                int closeParen = line.indexOf(')');
                int dashIndex = line.indexOf('-', closeParen);
                if (dashIndex == -1) dashIndex = line.indexOf('–', closeParen);
                if (dashIndex == -1) dashIndex = line.indexOf('—', closeParen);
                
                if (openParen > 0 && closeParen > openParen && dashIndex > closeParen) {
                    String term = line.substring(0, openParen).trim();
                    String definition = line.substring(dashIndex + 1).trim();
                    if (isValidDefinition(term, definition)) {
                        definitions.put(term, definition + " [Source: " + filename + "]");
                    }
                }
            }
        }
        
        // Pattern 3: Glossary or definition sections
        definitions.putAll(extractFromGlossarySection(text, filename));
        
        // Pattern 4: Bold/emphasized terms followed by definitions (for markdown)
        if (filename.toLowerCase().endsWith(".md")) {
            definitions.putAll(extractMarkdownDefinitions(text, filename));
        }
        
        return definitions;
    }
    
    /**
     * Extract definitions from glossary-like sections
     */
    private Map<String, String> extractFromGlossarySection(String text, String filename) {
        Map<String, String> definitions = new HashMap<>();
        
        // Look for sections that might contain definitions
        String[] sections = text.split("(?i)(глоссарий|словарь|определения|glossary|definitions|терминология)");
        
        if (sections.length > 1) {
            // Process the section after the glossary header
            String glossarySection = sections[1];
            String[] lines = glossarySection.split("\n");
            
            for (int i = 0; i < Math.min(lines.length, 100); i++) { // Limit to first 100 lines of glossary
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                
                // Stop if we hit another major section
                if (line.matches("(?i)^(глава|chapter|раздел|section|часть|part).*")) {
                    break;
                }
                
                if (line.matches("^[А-ЯЁA-Z].+[:–—-].+")) {
                    String[] parts = line.split("[:–—-]", 2);
                    if (parts.length == 2) {
                        String term = parts[0].trim();
                        String definition = parts[1].trim();
                        if (isValidDefinition(term, definition)) {
                            definitions.put(term, definition + " [Source: " + filename + " - Glossary]");
                        }
                    }
                }
            }
        }
        
        return definitions;
    }
    
    /**
     * Extract definitions from markdown format
     */
    private Map<String, String> extractMarkdownDefinitions(String text, String filename) {
        Map<String, String> definitions = new HashMap<>();
        
        String[] lines = text.split("\n");
        
        for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i].trim();
            String nextLine = lines[i + 1].trim();
            
            // Pattern: **Term** followed by definition
            if (line.matches("^\\*\\*[А-ЯЁA-Z].+\\*\\*$") && !nextLine.isEmpty()) {
                String term = line.replaceAll("\\*\\*", "").trim();
                String definition = nextLine;
                
                // Collect multiple lines if they seem to be part of the definition
                StringBuilder defBuilder = new StringBuilder(definition);
                for (int j = i + 2; j < lines.length; j++) {
                    String followingLine = lines[j].trim();
                    if (followingLine.isEmpty() || followingLine.startsWith("#") || followingLine.startsWith("**")) {
                        break;
                    }
                    defBuilder.append(" ").append(followingLine);
                }
                
                definition = defBuilder.toString().trim();
                if (isValidDefinition(term, definition)) {
                    definitions.put(term, definition + " [Source: " + filename + "]");
                }
            }
        }
        
        return definitions;
    }
    
    /**
     * Validate if a term and definition pair is worth keeping
     */
    private boolean isValidDefinition(String term, String definition) {
        if (term == null || definition == null) return false;
        
        term = term.trim();
        definition = definition.trim();
        
        // Basic validation rules
        if (term.length() < 2 || term.length() > 100) return false;
        if (definition.length() < 10 || definition.length() > 1000) return false;
        
        // Skip obvious non-definitions
        if (term.matches(".*\\d{4}.*")) return false; // Years
        if (term.toLowerCase().matches(".*(страница|page|глава|chapter|раздел|section).*")) return false;
        if (definition.toLowerCase().startsWith("см.") || definition.toLowerCase().startsWith("see")) return false;
        
        // Skip if definition is just a number or very short
        if (definition.matches("^\\d+[\\s\\w]{0,5}$")) return false;
        
        return true;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Extract a new trading strategy by scanning files and creating a JSON file based on the schema.
     * This method scans the provided file, extracts strategy information, and creates
     * a properly formatted JSON strategy file.
     * 
     * @param fileName Name of the file to scan for strategy information
     * @return Result message indicating success or failure
     */
    public String extractStrategy(String fileName) {
        try {
            // Read the input file
            String content = readFile(fileName);
            if (content.startsWith("Error") || content.startsWith("File not found")) {
                return "Cannot read input file: " + content;
            }
            
            // Create a basic strategy template
            ObjectNode strategy = objectMapper.createObjectNode();
            
            // Generate strategy ID from filename and timestamp
            String baseFileName = FilenameUtils.getBaseName(fileName);
            String strategyId = baseFileName.toLowerCase().replaceAll("[^a-z0-9]", "-") + 
                              "-" + System.currentTimeMillis();
            
            strategy.put("strategyId", strategyId);
            strategy.put("name", extractStrategyName(content, baseFileName));
            strategy.put("description", extractStrategyDescription(content));
            
            // Create conditions structure
            ObjectNode conditions = objectMapper.createObjectNode();
            ObjectNode entry = objectMapper.createObjectNode();
            entry.set("long", objectMapper.createArrayNode());
            entry.set("short", objectMapper.createArrayNode());
            conditions.set("entry", entry);
            
            ObjectNode exit = objectMapper.createObjectNode();
            conditions.set("exit", exit);
            strategy.set("conditions", conditions);
            
            // Create indicators structure
            ObjectNode indicators = objectMapper.createObjectNode();
            indicators.set("required", objectMapper.createArrayNode());
            indicators.set("optional", objectMapper.createArrayNode());
            strategy.set("indicators", indicators);
            
            // Extract basic information from content
            populateStrategyFromContent(strategy, content);
            
            // Generate output filename
            String outputFileName = "strategy-" + baseFileName.toLowerCase() + ".json";
            
            // Write the strategy JSON file
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(strategy);
            String writeResult = writeFile(outputFileName, jsonString);
            
            if (writeResult.startsWith("Error")) {
                return "Failed to write strategy file: " + writeResult;
            }
            
            // Update metadata for the strategy file
            updateFileMetadata(outputFileName, 
                "Trading strategy generated from " + fileName, 
                Arrays.asList("strategy", "trading", "json"),
                "strategy",
                "Auto-generated trading strategy based on " + fileName);
            
            return "Successfully created trading strategy file: " + outputFileName + 
                   "\nStrategy ID: " + strategyId + 
                   "\nBased on: " + fileName;
            
        } catch (Exception e) {
            logger.error("Error creating strategy from file: " + fileName, e);
            return "Error creating strategy: " + e.getMessage();
        }
    }
    
    /**
     * Extract strategy name from content or use filename as fallback
     */
    private String extractStrategyName(String content, String baseFileName) {
        // Look for common strategy name patterns
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            
            // Look for strategy/стратегия followed by name
            if (line.toLowerCase().matches(".*(strategy|стратегия).*:") && line.length() < 100) {
                return line.replaceAll("(?i)(strategy|стратегия)[:\\s]*", "").trim();
            }
            
            // Look for title-like patterns
            if (line.matches("^[A-ZА-Я][\\w\\s]{5,50}$") && !line.toLowerCase().contains("chapter")) {
                return line;
            }
        }
        
        // Fallback to formatted filename
        String formattedName = baseFileName.replaceAll("[-_]", " ");
        // Convert to title case manually
        StringBuilder titleCase = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : formattedName.toLowerCase().toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                titleCase.append(c);
            } else if (capitalizeNext) {
                titleCase.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                titleCase.append(c);
            }
        }
        return titleCase.toString() + " Strategy";
    }
    
    /**
     * Extract strategy description from content - now generates concrete condition descriptions
     */
    private String extractStrategyDescription(String content) {
        StringBuilder description = new StringBuilder();
        String lowerContent = content.toLowerCase();
        
        List<String> longConditions = new ArrayList<>();
        List<String> shortConditions = new ArrayList<>();
        List<String> exitConditions = new ArrayList<>();
        
        // Analyze content for long entry conditions
        if (lowerContent.contains("rsi") || lowerContent.contains("relative strength")) {
            if (lowerContent.contains("oversold") || lowerContent.contains("< 30") || lowerContent.contains("below 30")) {
                longConditions.add("RSI(14) < 30");
            }
        }
        
        if (lowerContent.contains("moving average") || lowerContent.contains("sma") || lowerContent.contains("ema")) {
            if (lowerContent.contains("price >") || lowerContent.contains("above")) {
                longConditions.add("close price > SMA(50)");
            }
        }
        
        if (lowerContent.contains("volume")) {
            longConditions.add("volume > 1.5x SMA(20)");
        }
        
        // Analyze content for short entry conditions
        if (lowerContent.contains("rsi") || lowerContent.contains("relative strength")) {
            if (lowerContent.contains("overbought") || lowerContent.contains("> 70") || lowerContent.contains("above 70")) {
                shortConditions.add("RSI(14) > 70");
            }
        }
        
        if (lowerContent.contains("moving average") || lowerContent.contains("sma") || lowerContent.contains("ema")) {
            if (lowerContent.contains("price <") || lowerContent.contains("below")) {
                shortConditions.add("close price < SMA(50)");
            }
        }
        
        // Build entry conditions description
        if (!longConditions.isEmpty()) {
            description.append("Long when ").append(String.join(" and ", longConditions)).append(". ");
        }
        
        if (!shortConditions.isEmpty()) {
            description.append("Short when ").append(String.join(" and ", shortConditions)).append(". ");
        }
        
        if (longConditions.isEmpty() && shortConditions.isEmpty()) {
            description.append("Entry signals to be defined based on technical analysis. ");
        }
        
        // Analyze exit conditions
        if (lowerContent.contains("stop loss") || lowerContent.contains("stop-loss")) {
            if (lowerContent.contains("%")) {
                // Try to extract percentage
                String[] words = content.split("\\s+");
                for (int i = 0; i < words.length - 1; i++) {
                    if (words[i].toLowerCase().contains("stop") && words[i+1].contains("%")) {
                        exitConditions.add("stop loss at " + words[i+1] + " from entry");
                        break;
                    }
                }
            }
            if (exitConditions.isEmpty()) {
                exitConditions.add("stop loss at 2% below entry");
            }
        }
        
        if (lowerContent.contains("take profit") || lowerContent.contains("target")) {
            if (lowerContent.contains("%")) {
                String[] words = content.split("\\s+");
                for (int i = 0; i < words.length - 1; i++) {
                    if (words[i].toLowerCase().contains("profit") && words[i+1].contains("%")) {
                        exitConditions.add("take profit at " + words[i+1] + " above entry");
                        break;
                    }
                }
            }
            if (exitConditions.stream().noneMatch(c -> c.contains("profit"))) {
                exitConditions.add("take profit at 5% above entry");
            }
        }
        
        // Look for RSI exit conditions
        if (lowerContent.contains("rsi") && lowerContent.contains("exit")) {
            if (lowerContent.contains("> 70") || lowerContent.contains("above 70")) {
                exitConditions.add("exit long when RSI(14) > 70");
            }
            if (lowerContent.contains("< 30") || lowerContent.contains("below 30")) {
                exitConditions.add("exit short when RSI(14) < 30");
            }
        }
        
        // Add exit conditions
        if (!exitConditions.isEmpty()) {
            description.append("Exit: ").append(String.join(", ", exitConditions)).append(". ");
        } else {
            description.append("Exit: use appropriate risk management. ");
        }
        
        // Add risk management if mentioned
        if (lowerContent.contains("risk") && lowerContent.contains("position")) {
            description.append("Risk management: limit position size and maintain risk-reward ratio.");
        }
        
        return description.toString();
    }
    
    /**
     * Populate strategy with information extracted from content
     */
    private void populateStrategyFromContent(ObjectNode strategy, String content) {
        // This is a basic implementation - can be enhanced to extract
        // actual conditions and indicators from text
        
        // Look for common trading terms and create sample conditions
        String lowerContent = content.toLowerCase();
        
        // Add sample RSI condition if RSI is mentioned
        if (lowerContent.contains("rsi") || lowerContent.contains("relative strength")) {
            addSampleRSICondition(strategy);
        }
        
        // Add sample moving average condition if MA is mentioned
        if (lowerContent.contains("moving average") || lowerContent.contains("ma") || 
            lowerContent.contains("sma") || lowerContent.contains("ema")) {
            addSampleMACondition(strategy);
        }
        
        // Add basic volume condition
        if (lowerContent.contains("volume") || lowerContent.contains("объем")) {
            addSampleVolumeCondition(strategy);
        }
    }
    
    private void addSampleRSICondition(ObjectNode strategy) {
        try {
            ObjectNode rsiCondition = objectMapper.createObjectNode();
            rsiCondition.put("type", "comparison");
            rsiCondition.put("operator", "<");
            
            ObjectNode left = objectMapper.createObjectNode();
            left.put("type", "indicator");
            left.put("name", "RSI");
            left.put("period", 14);
            
            ObjectNode right = objectMapper.createObjectNode();
            right.put("type", "value");
            right.put("value", 30);
            
            rsiCondition.set("left", left);
            rsiCondition.set("right", right);
            
            ((ArrayNode) strategy.get("conditions").get("entry").get("long")).add(rsiCondition);
            
            // Add RSI to required indicators
            ObjectNode rsiIndicator = objectMapper.createObjectNode();
            rsiIndicator.put("name", "RSI");
            rsiIndicator.put("period", 14);
            ((ArrayNode) strategy.get("indicators").get("required")).add(rsiIndicator);
            
        } catch (Exception e) {
            logger.warn("Failed to add RSI condition", e);
        }
    }
    
    private void addSampleMACondition(ObjectNode strategy) {
        try {
            ObjectNode maCondition = objectMapper.createObjectNode();
            maCondition.put("type", "comparison");
            maCondition.put("operator", ">");
            
            ObjectNode left = objectMapper.createObjectNode();
            left.put("type", "price");
            left.put("field", "close");
            
            ObjectNode right = objectMapper.createObjectNode();
            right.put("type", "indicator");
            right.put("name", "SMA");
            right.put("period", 50);
            
            maCondition.set("left", left);
            maCondition.set("right", right);
            
            ((ArrayNode) strategy.get("conditions").get("entry").get("long")).add(maCondition);
            
            // Add SMA to required indicators
            ObjectNode smaIndicator = objectMapper.createObjectNode();
            smaIndicator.put("name", "SMA");
            smaIndicator.put("period", 50);
            ((ArrayNode) strategy.get("indicators").get("required")).add(smaIndicator);
            
        } catch (Exception e) {
            logger.warn("Failed to add MA condition", e);
        }
    }
    
    private void addSampleVolumeCondition(ObjectNode strategy) {
        try {
            ObjectNode volumeCondition = objectMapper.createObjectNode();
            volumeCondition.put("type", "comparison");
            volumeCondition.put("operator", ">");
            
            ObjectNode left = objectMapper.createObjectNode();
            left.put("type", "price");
            left.put("field", "volume");
            
            ObjectNode right = objectMapper.createObjectNode();
            right.put("type", "indicator");
            right.put("name", "SMA_volume");
            right.put("period", 20);
            right.put("multiplier", 1.5);
            
            volumeCondition.set("left", left);
            volumeCondition.set("right", right);
            
            ((ArrayNode) strategy.get("conditions").get("entry").get("long")).add(volumeCondition);
            
            // Add volume SMA to optional indicators
            ObjectNode volumeIndicator = objectMapper.createObjectNode();
            volumeIndicator.put("name", "SMA_volume");
            volumeIndicator.put("period", 20);
            ((ArrayNode) strategy.get("indicators").get("optional")).add(volumeIndicator);
            
        } catch (Exception e) {
            logger.warn("Failed to add volume condition", e);
        }
    }
}
