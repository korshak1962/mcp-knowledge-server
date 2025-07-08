@echo off
echo 🔧 FIXING MCP KNOWLEDGE SERVER PROTOCOL ERRORS
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
echo 🔨 Compiling with fixes...
mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo.
echo 📦 Building JAR with protocol fixes...
mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ BUILD SUCCESSFUL!
    echo.
    echo 🔧 FIXES APPLIED:
    echo    ✓ Fixed MCP protocol response format
    echo    ✓ Added proper ID handling for all responses
    echo    ✓ Fixed JSON structure validation
    echo    ✓ Added response validation before sending
    echo    ✓ Improved error handling
    echo.
    echo 🚀 NEXT STEPS:
    echo    1. Restart Claude Desktop
    echo    2. Test the knowledge server tools
    echo    3. Budget analysis should now be accessible
    echo.
    echo 📂 Generated files:
    dir /b target\*.jar
) else (
    echo ❌ Package build failed
)

echo.
pause
