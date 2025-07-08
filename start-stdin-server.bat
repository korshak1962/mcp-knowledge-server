@echo off
echo Starting MCP Knowledge Server (stdin/stdout version for Claude Desktop)...
echo.

echo Checking Java version...
java -version
echo.

echo Building the project...
mvn clean install -q
echo.

if %ERRORLEVEL% EQU 0 (
    echo Build successful! Starting the STDIN server...
    echo.
    echo This server communicates via stdin/stdout for Claude Desktop
    echo Add this to your Claude Desktop MCP configuration:
    echo.
    echo   "mcp-knowledge-server": {
    echo     "command": "java",
    echo     "args": ["-cp", "target/mcp-knowledge-server-1.0.0.jar", "com.korshak.mcpserver.McpStdinServerApplication", "--spring.profiles.active=stdin"],
    echo     "cwd": "%CD%"
    echo   }
    echo.
    echo Starting server now...
    echo.
    java -cp target/mcp-knowledge-server-1.0.0.jar com.korshak.mcpserver.stdin.McpStdinServerApplication --spring.profiles.active=stdin
) else (
    echo Build failed! Please check the error messages above.
    pause
)
