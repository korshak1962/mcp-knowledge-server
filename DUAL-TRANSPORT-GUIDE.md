# MCP Knowledge Server - Dual Transport Support

This project provides **two versions** of the MCP Knowledge Server with different communication methods:

## 🌐 **Version 1: Web/WebSocket Server**
**Best for:** Testing, development, web integration

- **Transport:** WebSocket (ws://localhost:8080/mcp)
- **Additional:** REST API endpoints
- **Use case:** Web clients, testing, debugging

## 💻 **Version 2: Stdin/Stdout Server** 
**Best for:** Claude Desktop integration

- **Transport:** stdin/stdout communication
- **Use case:** Claude Desktop MCP configuration
- **Performance:** Lower overhead, direct communication

---

## 🚀 **Quick Start Guide**

### **Option A: Web/WebSocket Version**

1. **Start the web server:**
   ```bash
   start-web-server.bat
   ```
   
2. **Test the endpoints:**
   ```bash
   curl http://localhost:8080/api/knowledge/health
   ```

3. **WebSocket MCP endpoint:**
   ```
   ws://localhost:8080/mcp
   ```

### **Option B: Stdin/Stdout Version (Claude Desktop)**

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Add to Claude Desktop config:**
   
   Open Claude Desktop settings and add this MCP server configuration:
   ```json
   {
     "mcpServers": {
       "mcp-knowledge-server": {
         "command": "java",
         "args": [
           "-cp", 
           "D:\\mcp-knowledge-server\\target\\mcp-knowledge-server-1.0.0.jar",
           "com.korshak.mcpserver.McpStdinServerApplication",
           "--spring.profiles.active=stdin"
         ],
         "cwd": "D:\\mcp-knowledge-server"
       }
     }
   }
   ```

3. **Test the stdin version:**
   ```bash
   test-stdin-server.bat
   ```

---

## 🔧 **Available Scripts**

| Script | Purpose |
|--------|---------|
| `start-web-server.bat` | Start web/WebSocket version |
| `start-stdin-server.bat` | Start stdin version (shows config) |
| `test-stdin-server.bat` | Test stdin version with sample messages |
| `start-server.bat` | Legacy - starts web version |

---

## 📋 **Both Versions Support Same Tools**

1. **`list_files_with_metadata`** - Smart file listing with metadata
2. **`search_files_by_metadata`** - Search by tags, descriptions, categories
3. **`get_knowledge_store_overview`** - Store statistics and categories
4. **`read_file`** - Read file content
5. **`update_file_metadata`** - Add descriptions, tags, summaries
6. **`get_files_by_category`** - Browse by file type
7. **`write_file`** - Create/update files

---

## 🎯 **Which Version to Use?**

### **For Claude Desktop:** 
✅ Use **stdin version** - Add to MCP config for seamless integration

### **For Development/Testing:**
✅ Use **web version** - Easy testing with REST API and WebSocket tools

### **For Web Integration:**
✅ Use **web version** - Full REST API and WebSocket support

---

## 🔄 **Switching Between Versions**

Both versions use the same:
- ✅ Knowledge store folder (`./knowledgeStore`)
- ✅ Metadata files (`file-metadata.json`)
- ✅ MCP protocol and tools
- ✅ File processing capabilities

You can run both simultaneously (different ports/transports) or switch between them as needed!

---

## 🛠 **Claude Desktop Integration Example**

Once configured in Claude Desktop, you can use natural language:

```
"List my knowledge store files with metadata and help me find budget documents"
```

Claude will automatically:
1. Call `list_files_with_metadata` to see what's available
2. Use metadata to identify relevant files  
3. Read summaries for large files first
4. Provide intelligent analysis based on metadata

Perfect for managing large knowledge bases efficiently! 🚀
