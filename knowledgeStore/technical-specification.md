# Technical Specification: MCP Protocol Implementation

## Protocol Overview
The Model Context Protocol (MCP) enables secure, standardized communication between AI systems and external data sources.

## Implementation Details

### Supported Methods
- `initialize` - Establishes connection with protocol version negotiation
- `tools/list` - Returns available tools for file operations  
- `tools/call` - Executes specific file operation tools

### Tool Definitions

#### Core Tools
1. **list_files_with_metadata**
   - Purpose: List all files with rich metadata
   - Parameters: None
   - Returns: File list with categories, tags, size warnings

2. **read_file** 
   - Purpose: Extract content from files
   - Parameters: filename (string)
   - Returns: Extracted text content

3. **search_files_by_metadata**
   - Purpose: Search by file metadata  
   - Parameters: query (string)
   - Returns: Matching files with metadata

#### Advanced Tools
4. **get_knowledge_store_overview**
   - Purpose: Statistics and categorization overview
   - Parameters: None
   - Returns: File counts, categories, storage stats

5. **update_file_metadata**
   - Purpose: Enhance file metadata
   - Parameters: filename, description, tags, category, summary
   - Returns: Success confirmation

### Error Handling
- Standard JSON-RPC 2.0 error responses
- Descriptive error messages for debugging
- Graceful degradation for unsupported formats

### Security Model
- File path validation prevents directory traversal
- Sandboxed file operations within knowledge store
- Input sanitization for all parameters

## Performance Characteristics
- Average response time: 100-500ms for small files
- Large file handling: Progressive loading with warnings
- Concurrent request support via Spring Boot threading

## Format Support Matrix
| Format | Support Level | Library Used | Notes |
|--------|---------------|--------------|-------|
| PDF | Full | Apache PDFBox | Native text extraction |
| EPUB | Full | JSoup + ZIP | Chapter-aware parsing |
| TXT/MD | Full | Native Java | Direct string reading |
| DOCX | Good | Apache Tika | Office document support |
| Images | Metadata | Apache Tika | File info, no OCR |
| DjVu | Limited | External tools | Requires djvutxt installation |

## Integration Guidelines
- WebSocket endpoint: `ws://localhost:8080/mcp`
- Stdin/stdout: Direct process communication
- REST API: `http://localhost:8080/api/knowledge/` (testing only)
