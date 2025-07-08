@echo off
echo Starting MCP Knowledge Server (stdin version)...
echo.

REM Check if JAR exists
if not exist "target\mcp-knowledge-server-1.0.0-stdin.jar" (
    echo JAR file not found. Building project...
    call mvn clean package -DskipTests
    if %ERRORLEVEL% NEQ 0 (
        echo Build failed!
        pause
        exit /b 1
    )
)

echo.
echo Starting server with correct package name...
java -cp target/mcp-knowledge-server-1.0.0-stdin.jar com.korshak.mcpserver.stdin.McpStdinServerApplication --spring.profiles.active=stdin
