# Enhanced MCP Knowledge Server with Metadata

## 🚀 **New Metadata Features**

Your MCP Knowledge Server now has powerful metadata capabilities that help Claude make smart decisions about which files to use, especially for large files that exceed context limits.

### **🎯 How This Solves Your Problem**

1. **Smart File Discovery**: Claude can now see file descriptions, categories, and summaries without reading full content
2. **Large File Handling**: Files are marked as "large" with warnings and summaries for quick understanding
3. **Intelligent Search**: Search by metadata (tags, descriptions) instead of just content
4. **Category Organization**: Files are auto-categorized (document, text, image, etc.)

### **📋 New MCP Tools Available**

1. **`list_files_with_metadata`** - Shows rich metadata for all files
2. **`search_files_by_metadata`** - Search by description, tags, category, summary
3. **`get_files_by_category`** - Get all files in a specific category
4. **`get_knowledge_store_overview`** - Overview with statistics and categories
5. **`update_file_metadata`** - Add descriptions, tags, summaries to files

### **💡 Usage Examples for Claude**

**Smart Discovery:**
```
"List files with metadata to see what's available, then help me find budget documents"
```

**Category-based:**
```
"Show me all documents in the 'report' category and summarize the key points"
```

**Large File Handling:**
```
"I need info about Q4 projections. Check the metadata first to find relevant files, and if they're large, read their summaries instead of full content"
```

### **🔧 Setting Up Metadata**

The server automatically creates basic metadata, but you can enhance it:

**Via MCP Tool:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "update_file_metadata",
    "arguments": {
      "filename": "Q4-budget.pdf",
      "description": "Quarterly budget projections and analysis",
      "tags": ["budget", "Q4", "financial", "analysis"],
      "category": "document",
      "summary": "Contains revenue forecasts, expense breakdowns, and strategic recommendations for Q4. Key points include 15% revenue growth target and cost optimization initiatives."
    }
  }
}
```

**Via REST API:**
```bash
curl -X PUT http://localhost:8080/api/knowledge/files/Q4-budget.pdf/metadata \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Quarterly budget analysis",
    "tags": ["budget", "financial"],
    "category": "document",
    "summary": "Revenue forecasts and expense analysis"
  }'
```

### **🤖 How Claude Benefits**

- **Context Efficiency**: Claude reads metadata first, then decides which files to fully read
- **Large File Strategy**: For 50KB+ files, Claude can read summaries instead of full content
- **Smart Filtering**: Find relevant files by metadata before content search
- **Category Navigation**: Explore files by type (documents, images, spreadsheets)

### **📊 File Size Awareness**

Files over 50KB are marked as "large" with ⚠️ warnings. Claude will:
1. Check metadata first
2. Read summary if available
3. Only read full content if specifically needed

This makes your knowledge store much more efficient and intelligent!
