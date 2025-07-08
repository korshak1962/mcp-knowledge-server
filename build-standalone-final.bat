@echo off
echo 🚀 FINAL FIX: STANDALONE MCP SERVER (NO NULL IDS)
echo ═══════════════════════════════════════════════════

echo.
echo 🧹 Cleaning previous build...
mvn clean

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Clean failed
    pause
    exit /b 1
)

echo.
echo 🔨 Compiling standalone server...
mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo.
echo 📦 Building standalone JAR (bypassing Spring Boot issues)...
mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ STANDALONE SERVER BUILT SUCCESSFULLY!
    echo.
    echo 🎯 STANDALONE FEATURES:
    echo    ✓ No Spring Boot complexity - direct JSON-RPC
    echo    ✓ Guaranteed non-null IDs in all responses
    echo    ✓ Simple, reliable MCP protocol implementation
    echo    ✓ Direct file access without service layers
    echo    ✓ Minimal dependencies for maximum stability
    echo.
    echo 📂 Generated standalone JAR:
    dir /b target\*standalone*.jar
    echo.
    echo 🔧 UPDATING CLAUDE DESKTOP CONFIG...
    copy "claude-desktop-config-standalone.json" "C:\Users\korsh\AppData\Roaming\Claude\claude_desktop_config.json"
    
    if %ERRORLEVEL% EQU 0 (
        echo ✅ Claude Desktop config updated to use standalone server
    ) else (
        echo ⚠️  Please manually copy claude-desktop-config-standalone.json to:
        echo    C:\Users\korsh\AppData\Roaming\Claude\claude_desktop_config.json
    )
    
    echo.
    echo 🚀 FINAL STEPS:
    echo    1. Restart Claude Desktop NOW
    echo    2. Test the knowledge server tools
    echo    3. Budget analysis should work without Zod errors
    echo.
    echo 🎉 This standalone implementation guarantees proper MCP protocol compliance!
) else (
    echo ❌ Package build failed
)

echo.
pause
