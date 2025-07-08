@echo off
echo Starting MCP Knowledge Server (Web/WebSocket version)...
echo.

echo Checking Java version...
java -version
echo.

echo Building the project...
mvn clean install -q
echo.

if %ERRORLEVEL% EQU 0 (
    echo Build successful! Starting the WEB server...
    echo.
    echo Server will be available at:
    echo - WebSocket MCP endpoint: ws://localhost:8080/mcp
    echo - REST API base: http://localhost:8080/api/knowledge/
    echo - Health check: http://localhost:8080/api/knowledge/health
    echo.
    echo Press Ctrl+C to stop the server
    echo.
    java -jar target/mcp-knowledge-server-1.0.0.jar --spring.profiles.active=default
) else (
    echo Build failed! Please check the error messages above.
    pause
)
