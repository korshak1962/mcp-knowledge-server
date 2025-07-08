# DjVu Support in MCP Knowledge Server

## 📚 **DjVu Format Support Added!**

Your MCP Knowledge Server now supports DjVu format with intelligent handling and fallback options.

### **🔍 What is DjVu?**
DjVu is a specialized format designed for:
- **Scanned documents** and books
- **Academic papers** and historical documents  
- **Mixed content** with both images and text
- **Efficient storage** of high-resolution scanned pages
- **OCR text layers** for searchable content

### **✨ DjVu Features in Your Server**

**🛠️ Smart Tool Detection:**
- Automatically detects if `djvutxt` tool is available
- Uses native DjVu tools for best text extraction
- Graceful fallback with helpful installation instructions

**📖 Content Extraction Options:**
1. **Primary:** DjVuLibre `djvutxt` tool (best quality)
2. **Fallback:** Apache Tika parsing  
3. **Information:** Detailed format explanation and alternatives

**🏷️ Intelligent Metadata:**
- Auto-categorized as "djvu"
- Smart tagging: "scanned", "book", "academic", "archive"
- Filename-based content detection

**⚠️ Limitation Handling:**
- Clear messages about tool requirements
- Installation instructions for all platforms
- Alternative workflow suggestions

### **🚀 Setup for Full DjVu Support**

**Windows:**
```bash
# Download and install DjVuLibre
# Visit: http://djvu.sourceforge.net/
# Or use chocolatey:
choco install djvulibre
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install djvulibre-bin
```

**macOS:**
```bash
# Install via Homebrew
brew install djvulibre
```

**Verify Installation:**
```bash
djvutxt --help
# Should show help information
```

### **🔧 How DjVu Processing Works**

**With DjVuLibre Tools:**
1. Server detects `.djvu` or `.djv` file
2. Calls `djvutxt filename.djvu` 
3. Extracts embedded text layer
4. Returns clean, structured text content

**Without Tools (Fallback):**
1. Provides detailed format information
2. Shows installation instructions
3. Attempts Tika extraction as backup
4. Suggests alternative workflows

### **📋 Example Outputs**

**Successful Extraction:**
```
DjVu Content:
=============

Chapter 1: Introduction

This is the extracted text from the DjVu file...
The text layer was embedded during scanning...
```

**Fallback Mode:**
```
📚 DjVu Format Information:
DjVu is a specialized format for scanned documents and books.

⚠️  Text Extraction Limitation:
To extract text from DjVu files, this server requires 'djvutxt' tool.

Install DjVuLibre tools to enable full text extraction:
- Windows: Download from http://djvu.sourceforge.net/
- Linux: sudo apt install djvulibre-bin
- macOS: brew install djvulibre

🔄 Fallback Options:
1. Convert DjVu to PDF using online tools
2. Use DjVu viewers with copy/paste functionality  
3. Install djvutxt tool for automatic text extraction
```

### **🎯 Use Cases**

**Academic Research:**
- Extract text from scanned academic papers
- Search across historical document collections
- Process digital library archives

**Book Digitization:**
- Extract text from scanned books
- Create searchable digital libraries
- Preserve historical texts

**Document Archives:**
- Process institutional document collections
- Extract searchable content from legacy files
- Maintain original quality with text access

### **📊 MCP Tools Integration**

**List with metadata:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "list_files_with_metadata", 
    "arguments": {}
  }
}
```

**Read DjVu content:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "read_file",
    "arguments": {
      "filename": "academic-paper.djvu"
    }
  }
}
```

**Find DjVu files:**
```json
{
  "method": "tools/call",
  "params": {
    "name": "get_files_by_category",
    "arguments": {
      "category": "djvu"
    }
  }
}
```

### **🔄 Alternative Workflows**

**If DjVuLibre unavailable:**
1. **Online Conversion:** Use services like CloudConvert to convert DjVu→PDF
2. **Viewer Copy/Paste:** Use DjVu viewers to manually extract text
3. **OCR Tools:** Use external OCR on DjVu page images
4. **Format Conversion:** Convert to other supported formats

### **🚀 Getting Started**

1. **Install DjVuLibre tools** (optional but recommended)
2. **Add .djvu files** to your `knowledgeStore/` folder  
3. **Test extraction** using `read_file` tool
4. **Use metadata** for smart file organization

Your server now supports: **PDF, EPUB, DjVu, text files, images, Office documents**, and many more formats! 📚✨

**Note:** DjVu support is most effective with DjVuLibre tools installed, but the server provides helpful guidance even without them.
