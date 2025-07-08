# Claude Desktop MCP Server Configuration

## 📋 **Step-by-Step Setup**

### **1. Build the Server First**
```bash
# Run this in your project directory
cd D:\mcp-knowledge-server
mvn clean package -DskipTests
```

### **2. Find Claude Desktop Config File**

**Windows:**
```
%APPDATA%\Claude\claude_desktop_config.json
```

**macOS:**
```
~/Library/Application Support/Claude/claude_desktop_config.json
```

### **3. Add This Configuration**

Open the config file and add this MCP server configuration:

```json
{
  "mcpServers": {
    "mcp-knowledge-server": {
      "command": "java",
      "args": [
        "-cp", 
        "D:\\mcp-knowledge-server\\target\\mcp-knowledge-server-1.0.0.jar",
        "com.korshak.mcpserver.stdin.McpStdinServerApplication",
        "--spring.profiles.active=stdin"
      ],
      "cwd": "D:\\mcp-knowledge-server"
    }
  }
}
```

**⚠️ Important:** 
- Change `D:\\mcp-knowledge-server` to your actual project path
- Use double backslashes (`\\`) in Windows paths
- Make sure the JAR file exists in the target folder

### **4. Restart Claude Desktop**

After adding the configuration:
1. Close Claude Desktop completely
2. Restart the application  
3. The MCP server should connect automatically

### **5. Test Connection**

Try asking Claude Desktop:
```
"What files do you have access to in my knowledge store?"
```

Claude should respond by listing the files using the MCP tools.

## 🧪 **Example Test Queries**

Once connected, try these questions:

### **Basic File Operations:**
```
"List all files in my knowledge store with their metadata"
"Give me an overview of my knowledge store"
"What categories of files do I have?"
```

### **Content Reading:**
```
"Read the welcome.md file and summarize it"
"What's in the setup-instructions.txt file?"
"Show me the content of the metadata-guide.md file"
```

### **Smart Search:**
```
"Search for files containing information about 'EPUB'"
"Find files in the 'text' category"
"Search my knowledge store for 'MCP' related content"
```

### **Metadata Management:**
```
"Update the metadata for welcome.md with a description: 'Introduction to the knowledge server features'"
"What files are marked as large files?"
"Show me all files tagged with 'guide'"
```

## 🔍 **Troubleshooting**

### **If the server doesn't connect:**

1. **Check the JAR exists:**
   ```bash
   dir D:\mcp-knowledge-server\target\*.jar
   ```

2. **Test the server manually:**
   ```bash
   cd D:\mcp-knowledge-server
   test-stdin-server.bat
   ```

3. **Check Claude Desktop logs:**
   - Look for MCP connection errors in Claude Desktop
   - Server logs will show in the terminal if run manually

4. **Verify Java installation:**
   ```bash
   java -version
   # Should show Java 17 or higher
   ```

### **Common Issues:**

- **Path problems:** Use absolute paths and double backslashes
- **Java not found:** Ensure Java is in system PATH
- **Port conflicts:** The stdin version doesn't use ports
- **Permission issues:** Run Claude Desktop as administrator if needed

## 🎯 **What to Expect**

When working correctly, Claude Desktop will:
- ✅ Automatically call MCP tools when you ask about files
- ✅ List files with rich metadata (size, category, tags)
- ✅ Read and summarize file contents
- ✅ Search across your knowledge store
- ✅ Provide intelligent file management

You'll see Claude making calls like:
- `list_files_with_metadata` 
- `read_file`
- `search_files_by_metadata`
- `get_knowledge_store_overview`

## 📁 **Your Test Files**

The knowledgeStore already contains:
- `welcome.md` - Server introduction
- `setup-instructions.txt` - Setup guide  
- `metadata-guide.md` - Enhanced metadata features
- `epub-support-guide.md` - EPUB format support
- `djvu-support-guide.md` - DjVu format info

Perfect for testing the server's capabilities!
