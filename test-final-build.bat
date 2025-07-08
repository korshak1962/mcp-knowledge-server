@echo off
echo Testing compilation with fixes...
echo.

echo Step 1: Clean compile
mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful!
    echo.
    echo Step 2: Full package build
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo 🎉 BUILD SUCCESSFUL!
        echo.
        echo ✅ Fixed issues:
        echo   - Changed javax.annotation to jakarta.annotation
        echo   - Downgraded PDFBox to stable 2.0.29 version
        echo   - RTF support via Apache Tika
        echo.
        echo Ready to run:
        echo   start-web-server.bat    (Web + REST API)
        echo   start-stdin-server.bat  (Claude Desktop)
        echo.
        echo Test with an RTF file:
        echo   1. Add RTF file to knowledgeStore/
        echo   2. Use MCP tool: read_file with your RTF filename
        echo.
    ) else (
        echo.
        echo ❌ Package build failed!
        echo Check the error messages above.
    )
) else (
    echo.
    echo ❌ Compilation still failing!
    echo Check the error messages above.
)

pause
