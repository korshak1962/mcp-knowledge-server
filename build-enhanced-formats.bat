@echo off
echo Building MCP Knowledge Server with EPUB + DjVu support...
echo.

echo ===============================================
echo  CLEANING AND COMPILING
echo ===============================================
mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo ❌ COMPILATION FAILED
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.

echo ===============================================
echo  BUILDING WITH ENHANCED FORMAT SUPPORT
echo ===============================================
mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 🎉 BUILD COMPLETED SUCCESSFULLY!
    echo.
    echo ===============================================
    echo  📚 SUPPORTED FORMATS
    echo ===============================================
    echo ✅ PDF Documents (.pdf) - Apache PDFBox
    echo ✅ EPUB eBooks (.epub) - Custom parser with JSoup  
    echo ✅ DjVu Documents (.djvu, .djv) - DjVuLibre integration
    echo ✅ Text Files (.txt, .md) - Native support
    echo ✅ Images (.jpg, .png, .gif) - Metadata extraction
    echo ✅ Office Docs (.docx, .xlsx, .pptx) - Apache Tika
    echo ✅ Many More - Apache Tika fallback
    echo.
    echo ===============================================
    echo  🔧 DJVU SETUP (OPTIONAL)
    echo ===============================================
    echo For best DjVu text extraction, install DjVuLibre:
    echo.
    echo Windows: Download from http://djvu.sourceforge.net/
    echo Linux:   sudo apt install djvulibre-bin
    echo macOS:    brew install djvulibre
    echo.
    echo Verify: djvutxt --help
    echo.
    echo ⚠️  Note: DjVu files work without this tool but with limited text extraction
    echo.
    echo ===============================================
    echo  📋 INTELLIGENT CATEGORIZATION
    echo ===============================================
    echo PDF files     → "document" category
    echo EPUB files    → "ebook" category  
    echo DjVu files    → "djvu" category
    echo Text files    → "text" category
    echo Images        → "image" category
    echo Office docs   → "document/spreadsheet/presentation"
    echo.
    echo ===============================================
    echo  🚀 READY TO USE
    echo ===============================================
    echo Generated JARs:
    dir /b target\*.jar 2>nul
    echo.
    echo Start servers:
    echo   Web version:   start-web-server.bat
    echo   Stdin version: start-stdin-server.bat
    echo.
    echo Test with sample files:
    echo   1. Add .epub, .djvu, .pdf files to knowledgeStore/
    echo   2. Use list_files_with_metadata to see smart categorization
    echo   3. Use read_file to extract content from any format
    echo   4. Use get_files_by_category to browse by type
    echo.
    echo 📚 Your knowledge server now handles books, documents, and scanned files! ✨
) else (
    echo ❌ BUILD FAILED - check errors above
)

echo.
pause
