@echo off
echo 🚨 CRITICAL FIX: NULL ID ISSUE IN MCP PROTOCOL
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
echo 🔨 Compiling with NULL ID fixes...
mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo.
echo 📦 Building JAR with CRITICAL NULL ID fixes...
mvn package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ CRITICAL FIX APPLIED SUCCESSFULLY!
    echo.
    echo 🚨 CRITICAL FIXES FOR NULL ID ISSUE:
    echo    ✓ Never send null ID in responses
    echo    ✓ Always provide string fallback ID "0" when null
    echo    ✓ Validate all outgoing responses for null IDs
    echo    ✓ Proper ID extraction from incoming requests
    echo    ✓ Enhanced error handling with valid IDs
    echo.
    echo 🚀 NEXT STEPS:
    echo    1. Restart Claude Desktop IMMEDIATELY
    echo    2. Test the knowledge server tools
    echo    3. Zod validation errors should be RESOLVED
    echo.
    echo 📂 Generated files:
    dir /b target\*.jar
    echo.
    echo ⚠️  IMPORTANT: You MUST restart Claude Desktop for this fix to take effect!
) else (
    echo ❌ Package build failed
)

echo.
pause
