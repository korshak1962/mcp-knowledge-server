@echo off
echo Testing MCP Knowledge Server (stdin version)...
echo.

echo Building the project first...
mvn clean install -q

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting stdin server and sending test messages...
echo.

(
echo {"jsonrpc": "2.0", "id": "1", "method": "initialize", "params": {"protocolVersion": "2024-11-05", "clientInfo": {"name": "test-client", "version": "1.0.0"}}}
echo {"jsonrpc": "2.0", "id": "2", "method": "tools/list", "params": {}}
echo {"jsonrpc": "2.0", "id": "3", "method": "tools/call", "params": {"name": "get_knowledge_store_overview", "arguments": {}}}
echo {"jsonrpc": "2.0", "id": "4", "method": "tools/call", "params": {"name": "list_files_with_metadata", "arguments": {}}}
) | java -cp target/mcp-knowledge-server-1.0.0.jar com.korshak.mcpserver.stdin.McpStdinServerApplication --spring.profiles.active=stdin

echo.
echo Test completed. Check the JSON responses above.
pause
