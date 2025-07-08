# EPUB Support in MCP Knowledge Server

## 📚 **EPUB Format Now Fully Supported!**

Your MCP Knowledge Server can now read and process EPUB ebooks with dedicated support beyond the generic Apache Tika fallback.

### **🔍 What is EPUB?**
EPUB (Electronic Publication) is a widely-used ebook format that's essentially a ZIP archive containing:
- HTML/XHTML files (the actual book content)
- CSS stylesheets for formatting
- Images and other media
- Metadata files (OPF, NCX)

### **✨ Enhanced EPUB Features**

**📖 Smart Content Extraction:**
- Extracts text from all HTML/XHTML chapters
- Maintains chapter structure and titles
- Removes styling and scripts for clean text
- Handles multiple content files in reading order

**🏷️ Intelligent Metadata:**
- Auto-categorized as "ebook" 
- Smart tagging based on filename:
  - Fiction/novel books → "fiction" tag
  - Technical/programming → "technical" tag  
  - Tutorial/learning → "educational" tag
- Standard ebook tags: "epub", "ebook", "book"

**🛡️ Robust Error Handling:**
- Detects DRM-protected EPUB files
- Handles corrupted or malformed files
- Provides detailed error messages
- Graceful fallback to Tika if needed

### **🚀 How to Use**

**1. Add EPUB files to your knowledge store:**
```bash
# Copy your EPUB files
copy "my-ebook.epub" "D:\mcp-knowledge-server\knowledgeStore\"
```

**2. List files with metadata:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "list_files_with_metadata",
    "arguments": {}
  }
}
```

**3. Read EPUB content:**
```json
{
  "method": "tools/call", 
  "params": {
    "name": "read_file",
    "arguments": {
      "filename": "my-ebook.epub"
    }
  }
}
```

**4. Search ebooks by category:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "get_files_by_category", 
    "arguments": {
      "category": "ebook"
    }
  }
}
```

### **📋 Example Output**

When reading an EPUB, you'll get structured output like:

```
EPUB Content:
==================

Chapter: Introduction to Programming
-------------------
Welcome to the world of programming! This book will teach you...

Chapter: Variables and Data Types  
-------------------
In programming, variables are containers that store data values...

Chapter: Control Structures
-------------------
Control structures allow you to control the flow of your program...
```

### **🔧 Technical Details**

**Dependencies Added:**
- JSoup 1.17.2 for HTML parsing
- Built-in Java ZIP support for EPUB archive handling

**Supported EPUB Features:**
- ✅ EPUB 2.0 and 3.0 formats
- ✅ DRM-free EPUB files
- ✅ Multiple chapter files
- ✅ UTF-8 text encoding
- ✅ HTML content extraction

**Limitations:**
- ❌ DRM-protected EPUBs (will show error message)
- ❌ Image extraction (text content only)
- ❌ Complex formatting preservation

### **💡 Use Cases**

**Personal Library Management:**
- Index your ebook collection
- Search across multiple books
- Organize by genre/topic via metadata

**Research and Study:**
- Extract text from technical ebooks
- Search for specific concepts across books
- Create summaries of educational content

**Content Analysis:**
- Analyze writing patterns
- Extract quotes and passages
- Compare different books on same topic

Your knowledge server is now a powerful ebook management and analysis tool! 📚✨
