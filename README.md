# MCP Knowledge Server with Enhanced Metadata

A Model Context Protocol (MCP) server built with Spring Boot that provides intelligent access to a knowledge store with rich metadata capabilities for efficient file discovery and large file handling.

## 🚀 **Key Features**

- **Smart Metadata System**: Rich file descriptions, tags, categories, and summaries
- **Large File Intelligence**: Automatic detection and handling of files that exceed context limits
- **Multi-format Support**: Handles text files, PDFs, images, and more using Apache Tika
- **MCP Protocol**: Full MCP 2024-11-05 protocol implementation
- **WebSocket Interface**: Real-time communication via WebSocket
- **REST API**: Additional HTTP endpoints for testing and file uploads
- **Intelligent Search**: Search by content OR metadata (tags, descriptions, categories)
- **Category Auto-detection**: Automatic file categorization
- **Spring Boot**: Built on robust Spring Boot framework

## 🎯 **Problem Solved: Large File Context Management**

This server intelligently handles the challenge of large files exceeding Claude's context window:

- **Metadata First**: Claude can see file descriptions and summaries without reading full content
- **Size Warnings**: Files >50KB are flagged with recommendations to read summaries first
- **Smart Discovery**: Find relevant files through metadata search before content search
- **Category Navigation**: Organize and discover files by type

## 🚀 **Two Transport Options Available**

**📋 [See Dual Transport Guide](DUAL-TRANSPORT-GUIDE.md) for detailed setup**

### **Option 1: Web/WebSocket Version** (for testing/development)
- WebSocket: `ws://localhost:8080/mcp`
- REST API: `http://localhost:8080/api/knowledge/`
- Command: `start-web-server.bat`

### **Option 2: Stdin/Stdout Version** (for Claude Desktop)
- Direct stdin/stdout communication
- Add to Claude Desktop MCP configuration
- Command: `start-stdin-server.bat`

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building and Running

1. **Clone or navigate to the project directory**:
   ```bash
   cd D:\mcp-knowledge-server
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the server**:
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR directly:
   ```bash
   java -jar target/mcp-knowledge-server-1.0.0.jar
   ```

4. **Server will start on port 8080**

### Testing the Server

**Health Check**:
```bash
curl http://localhost:8080/api/knowledge/health
```

**List Files**:
```bash
curl http://localhost:8080/api/knowledge/files
```

**Upload a File**:
```bash
curl -X POST -F "file=@your-file.txt" http://localhost:8080/api/knowledge/upload
```

## MCP Protocol Usage

### WebSocket Endpoint
```
ws://localhost:8080/mcp
```

### Available Tools

1. **list_files**: List all files in the knowledge store
   ```json
   {
     "jsonrpc": "2.0",
     "id": "1",
     "method": "tools/call",
     "params": {
       "name": "list_files",
       "arguments": {}
     }
   }
   ```

2. **read_file**: Read the content of a specific file
   ```json
   {
     "jsonrpc": "2.0",
     "id": "2",
     "method": "tools/call",
     "params": {
       "name": "read_file",
       "arguments": {
         "filename": "welcome.md"
       }
     }
   }
   ```

3. **search_files**: Search for files containing specific text
   ```json
   {
     "jsonrpc": "2.0",
     "id": "3",
     "method": "tools/call",
     "params": {
       "name": "search_files",
       "arguments": {
         "query": "your search term"
       }
     }
   }
   ```

4. **get_file_info**: Get metadata about a file
   ```json
   {
     "jsonrpc": "2.0",
     "id": "4",
     "method": "tools/call",
     "params": {
       "name": "get_file_info",
       "arguments": {
         "filename": "welcome.md"
       }
     }
   }
   ```

5. **write_file**: Write content to a file
   ```json
   {
     "jsonrpc": "2.0",
     "id": "5",
     "method": "tools/call",
     "params": {
       "name": "write_file",
       "arguments": {
         "filename": "new-file.txt",
         "content": "Your content here"
       }
     }
   }
   ```

## Project Structure

```
D:\mcp-knowledge-server\
├── src/
│   ├── main/
│   │   ├── java/com/example/mcpserver/
│   │   │   ├── McpKnowledgeServerApplication.java
│   │   │   ├── McpWebSocketHandler.java
│   │   │   ├── controller/
│   │   │   │   └── KnowledgeStoreController.java
│   │   │   ├── handler/
│   │   │   │   └── McpProtocolHandler.java
│   │   │   ├── model/
│   │   │   │   ├── McpMessage.java
│   │   │   │   ├── McpError.java
│   │   │   │   ├── Tool.java
│   │   │   │   └── ToolCallResult.java
│   │   │   └── service/
│   │   │       └── KnowledgeStoreService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── knowledgeStore/          # Your knowledge files go here
│   ├── welcome.md
│   └── setup-instructions.txt
├── pom.xml
└── README.md
```

## Configuration

Edit `src/main/resources/application.properties` to customize:

- `server.port`: Server port (default: 8080)
- `knowledge.store.path`: Path to knowledge store (default: ./knowledgeStore)
- Logging levels and other Spring Boot settings

## Supported File Types

- **Text files**: .txt, .md
- **PDF files**: .pdf (using Apache PDFBox)
- **Images**: .jpg, .jpeg, .png, .gif (metadata extraction)
- **Office documents**: .docx, .xlsx, .pptx (using Apache Tika)
- **And many more** through Apache Tika's extensive format support

## Dependencies

- Spring Boot 3.2.0
- Apache PDFBox 3.0.1 (PDF processing)
- Apache Tika 2.9.1 (Multi-format document processing)
- Apache Commons IO 2.15.1 (File operations)
- Jackson (JSON processing)

## Development

To extend the server:

1. Add new tools in `McpProtocolHandler`
2. Implement business logic in `KnowledgeStoreService`
3. Add REST endpoints in `KnowledgeStoreController` if needed
4. Update the tool schemas in the `handleToolsList` method

## License

This project is provided as-is for educational and development purposes.
