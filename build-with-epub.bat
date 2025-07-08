@echo off
echo Building MCP Knowledge Server with EPUB support...
echo.

echo Cleaning and compiling...
mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilation successful!
    echo.
    echo Packaging with EPUB dependencies...
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo 🎉 Build completed successfully!
        echo.
        echo ===============================================
        echo  EPUB SUPPORT ADDED
        echo ===============================================
        echo ✅ Supported formats now include:
        echo    - PDF files (.pdf)
        echo    - EPUB ebooks (.epub) ← NEW!
        echo    - Text files (.txt, .md)
        echo    - Images (.jpg, .png, .gif)
        echo    - Office docs (.docx, .xlsx, .pptx)
        echo    - And many more via Apache Tika
        echo.
        echo ===============================================
        echo  EPUB FEATURES
        echo ===============================================
        echo ✅ Full text extraction from EPUB files
        echo ✅ Chapter-by-chapter reading
        echo ✅ HTML content parsing with JSoup
        echo ✅ Auto-categorization as "ebook"
        echo ✅ Smart tagging (fiction, technical, educational)
        echo ✅ DRM detection and error handling
        echo.
        echo ===============================================
        echo  READY TO TEST
        echo ===============================================
        echo 1. Add an EPUB file to knowledgeStore/ folder
        echo 2. Run: start-web-server.bat or start-stdin-server.bat
        echo 3. Use list_files_with_metadata to see EPUB metadata
        echo 4. Use read_file to extract EPUB content
        echo.
        echo Generated JARs:
        dir /b target\*.jar
    else (
        echo ❌ Package failed - check errors above
    )
) else (
    echo ❌ Compilation failed - check errors above
)

echo.
pause
