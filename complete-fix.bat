@echo off
echo ============================================
echo  MCP Knowledge Server - Complete Fix
echo ============================================
echo.

echo 1. Checking Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found! Please install Java 17 or higher.
    pause
    exit /b 1
)
echo.

echo 2. Checking Maven...
mvn -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found! Please install Maven.
    pause
    exit /b 1
)
echo.

echo 3. Cleaning previous builds...
if exist "target" rmdir /s /q target
echo.

echo 4. Building project...
mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo.

echo 5. Creating JAR packages...
mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Packaging failed!
    pause
    exit /b 1
)
echo.

echo 6. Listing generated JARs...
dir /b target\*.jar
echo.

echo 7. Testing stdin server startup...
echo Testing with a simple initialization...
timeout /t 2 /nobreak > nul
echo {"jsonrpc": "2.0", "method": "initialize", "params": {"protocolVersion": "2024-11-05", "capabilities": {}}, "id": 1} | java -cp target/mcp-knowledge-server-1.0.0-stdin.jar com.korshak.mcpserver.stdin.McpStdinServerApplication --spring.profiles.active=stdin

echo.
echo ============================================
echo  Build complete! Available scripts:
echo ============================================
echo  - start-stdin-server-fixed.bat (for Claude Desktop)
echo  - start-web-server.bat (for web interface)
echo.
pause
