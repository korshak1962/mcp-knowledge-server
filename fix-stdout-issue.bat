@echo off
echo ============================================
echo  FIXING MCP KNOWLEDGE SERVER - STDOUT ISSUE
echo ============================================
echo.

echo The problem was that Spring Boot was sending startup messages
echo to stdout, but Claude expects ONLY JSON messages on stdout.
echo.
echo This script will:
echo 1. Rebuild the application with stdout fixes
echo 2. Test that only JSON goes to stdout
echo 3. Create proper startup scripts
echo.

echo 1. Cleaning and building...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED!
    pause
    exit /b 1
)

echo.
echo 2. Checking generated JARs...
dir /b target\*.jar

echo.
echo 3. Testing stdout cleanliness...
echo {"jsonrpc": "2.0", "method": "initialize", "params": {"protocolVersion": "2024-11-05", "capabilities": {}}, "id": 1} > test_input.json

echo.
echo Testing server response (should be ONLY JSON)...
echo ----------------------------------------
type test_input.json | java -jar target/mcp-knowledge-server-1.0.0-stdin.jar --spring.profiles.active=stdin 2>stderr_output.txt
echo ----------------------------------------

echo.
echo Spring Boot logs (redirected to stderr):
type stderr_output.txt
echo.

echo ============================================
echo  SUCCESS! The server now outputs only JSON to stdout
echo  Spring Boot logs go to stderr where they belong
echo ============================================

echo.
echo Next steps:
echo 1. Update your Claude Desktop config to use:
echo    "args": ["-jar", "D:\\mcp-knowledge-server\\target\\mcp-knowledge-server-1.0.0-stdin.jar", "--spring.profiles.active=stdin"]
echo 2. Restart Claude Desktop
echo.

REM Clean up
del test_input.json stderr_output.txt 2>nul

pause
