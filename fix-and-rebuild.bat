@echo off
echo Fixing MCP Stdin Server and rebuilding...
echo.

echo Step 1: Clean build directory
mvn clean

echo.
echo Step 2: Compile all classes
mvn compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful!
    echo.
    echo Step 3: Package the application
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ✅ BUILD SUCCESSFUL!
        echo.
        echo Fixed:
        echo   - Moved McpStdinServerApplication to correct package location
        echo   - Fixed package declaration (com.korshak.mcpserver)
        echo   - Simplified stdin handler
        echo.
        echo Ready to test:
        echo   test-stdin-server.bat
        echo.
        echo Or configure Claude Desktop with:
        echo   "com.korshak.mcpserver.McpStdinServerApplication"
        echo.
    ) else (
        echo.
        echo ❌ Package build failed!
    )
) else (
    echo.
    echo ❌ Compilation failed!
)

pause
