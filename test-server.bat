@echo off
echo ============================================
echo  Testing MCP Stdin Server
echo ============================================
echo.

REM Check if JAR exists
if not exist "target\mcp-knowledge-server-1.0.0-stdin.jar" (
    echo JAR file not found. Building first...
    call complete-fix.bat
    if %ERRORLEVEL% NEQ 0 (
        echo Build failed! Cannot test.
        pause
        exit /b 1
    )
)

echo Testing MCP server with initialize message...
echo.

REM Create a temporary test file with MCP initialize message
echo {"jsonrpc": "2.0", "method": "initialize", "params": {"protocolVersion": "2024-11-05", "capabilities": {}}, "id": 1} > test_input.json

echo Input message:
type test_input.json
echo.
echo.

echo Server response:
echo ----------------------------------------
type test_input.json | java -cp target/mcp-knowledge-server-1.0.0-stdin.jar com.korshak.mcpserver.stdin.McpStdinServerApplication --spring.profiles.active=stdin
echo ----------------------------------------
echo.

REM Clean up
del test_input.json

echo.
echo Test completed!
pause
