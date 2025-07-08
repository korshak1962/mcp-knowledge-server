# Test Scenarios for Claude Desktop MCP Integration

## 🧪 **Comprehensive Test Suite**

Use these test queries with Claude Desktop to verify your MCP Knowledge Server is working correctly.

## 📋 **Phase 1: Basic Connection Tests**

### **Test 1: Server Connection**
```
"What MCP tools do you have available?"
```
**Expected:** Claude should list the 8 knowledge store tools

### **Test 2: File Discovery**
```
"What files do you have access to in my knowledge store?"
```
**Expected:** Claude calls `list_files` and shows available files

### **Test 3: Enhanced Metadata**
```
"List all files with their metadata including categories and tags"
```
**Expected:** Claude calls `list_files_with_metadata` and shows rich file information

## 📊 **Phase 2: Content Reading Tests**

### **Test 4: Read Specific File**
```
"Read the welcome.md file and tell me what this knowledge server can do"
```
**Expected:** Claude calls `read_file` and summarizes the server features

### **Test 5: Multiple File Reading**
```
"Read both the setup-instructions.txt and metadata-guide.md files and explain how to set up enhanced metadata"
```
**Expected:** Claude reads both files and provides a comprehensive setup guide

### **Test 6: Large File Handling**
```
"Which files in my knowledge store are marked as large files and why?"
```
**Expected:** Claude identifies files >50KB with size warnings

## 🔍 **Phase 3: Search and Discovery**

### **Test 7: Content Search**
```
"Search my knowledge store for information about EPUB files"
```
**Expected:** Claude calls `search_files` and finds EPUB-related content

### **Test 8: Metadata Search**
```
"Search by metadata for files tagged with 'guide' or in the 'text' category"
```
**Expected:** Claude uses `search_files_by_metadata` for smarter search

### **Test 9: Category Browsing**
```
"Show me all files in the 'text' category and what they contain"
```
**Expected:** Claude calls `get_files_by_category` and provides summaries

## 📈 **Phase 4: Analytics and Overview**

### **Test 10: Knowledge Store Overview**
```
"Give me a complete overview of my knowledge store including statistics and file types"
```
**Expected:** Claude calls `get_knowledge_store_overview` and presents stats

### **Test 11: File Analysis**
```
"Analyze my knowledge store and tell me what types of content I have and how it's organized"
```
**Expected:** Claude combines multiple tools for comprehensive analysis

### **Test 12: Recommendations**
```
"Based on my current files, what would you recommend I add to make this knowledge store more useful?"
```
**Expected:** Claude analyzes content and suggests improvements

## 🔧 **Phase 5: Metadata Management**

### **Test 13: Update Metadata**
```
"Update the metadata for welcome.md with the description 'Server introduction and feature overview' and add tags 'introduction' and 'features'"
```
**Expected:** Claude calls `update_file_metadata` and confirms update

### **Test 14: File Information**
```
"Get detailed information about the metadata-guide.md file including all its metadata"
```
**Expected:** Claude calls `get_file_info` and shows comprehensive details

### **Test 15: Smart Organization**
```
"Help me organize my knowledge store by suggesting better categories and tags for my files"
```
**Expected:** Claude analyzes files and suggests metadata improvements

## 🎯 **Phase 6: Real-World Scenarios**

### **Test 16: Research Assistant**
```
"I'm working on a project about document formats. Find all relevant information in my knowledge store and create a summary"
```
**Expected:** Claude searches across files and compiles relevant information

### **Test 17: File Management**
```
"Which files should I read first to understand how to use this knowledge server effectively?"
```
**Expected:** Claude prioritizes files based on content and metadata

### **Test 18: Content Creation**
```
"Create a new file called 'test-notes.md' with content about today's testing session"
```
**Expected:** Claude calls `write_file` to create new content

## 🚀 **Phase 7: Advanced Features**

### **Test 19: Format Support**
```
"What file formats does this knowledge server support and how does it handle each one?"
```
**Expected:** Claude reads format guides and explains PDF, EPUB, DjVu, etc.

### **Test 20: Integration Testing**
```
"Help me understand the difference between using the web version vs the stdin version of this server"
```
**Expected:** Claude reads setup documentation and explains both versions

## 📊 **Expected Tool Usage Patterns**

During testing, you should see Claude calling:
- `list_files_with_metadata` → For file discovery
- `read_file` → For content reading  
- `search_files_by_metadata` → For smart search
- `get_knowledge_store_overview` → For statistics
- `get_files_by_category` → For browsing by type
- `update_file_metadata` → For metadata management
- `write_file` → For content creation
- `get_file_info` → For detailed file information

## 🔍 **Troubleshooting Tests**

### **If Claude doesn't use MCP tools:**
- Restart Claude Desktop
- Check MCP configuration
- Verify server JAR exists

### **If tools fail:**
- Test server manually with `test-stdin-server.bat`
- Check Java installation  
- Verify file paths in configuration

### **If responses seem wrong:**
- Check knowledgeStore folder has the test files
- Verify file permissions
- Test with simpler queries first

## 🎉 **Success Indicators**

✅ Claude automatically discovers files without being told  
✅ Claude provides rich metadata (categories, tags, sizes)  
✅ Claude can read and summarize multiple file formats  
✅ Claude uses smart search instead of basic text matching  
✅ Claude can update file metadata and create new files  
✅ Claude combines information from multiple files intelligently  

Your MCP Knowledge Server is working perfectly when Claude feels like it has native access to a smart, searchable knowledge base! 🚀
