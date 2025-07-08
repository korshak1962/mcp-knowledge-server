@echo off
echo Claude Desktop MCP Configuration Helper
echo ========================================
echo.

echo This script will help you configure Claude Desktop to use your MCP Knowledge Server.
echo.

echo Step 1: Build the server first
echo ------------------------------
echo Building the MCP server...
mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed! Please fix build errors first.
    pause
    exit /b 1
)

echo ✅ Server built successfully!
echo.

echo Step 2: Locate Claude Desktop config
echo ------------------------------------
set CONFIG_DIR=%APPDATA%\Claude
set CONFIG_FILE=%CONFIG_DIR%\claude_desktop_config.json

echo Looking for Claude Desktop config at:
echo %CONFIG_FILE%
echo.

if not exist "%CONFIG_DIR%" (
    echo ❌ Claude Desktop config directory not found at:
    echo    %CONFIG_DIR%
    echo.
    echo Please install Claude Desktop first, then run this script again.
    pause
    exit /b 1
)

echo ✅ Claude Desktop directory found!
echo.

echo Step 3: Backup existing config (if any)
echo ---------------------------------------
if exist "%CONFIG_FILE%" (
    echo Backing up existing config...
    copy "%CONFIG_FILE%" "%CONFIG_FILE%.backup"
    echo ✅ Backup created: %CONFIG_FILE%.backup
) else (
    echo No existing config found - will create new one.
)
echo.

echo Step 4: Create MCP server configuration
echo ---------------------------------------
echo Creating configuration file...

(
echo {
echo   "mcpServers": {
echo     "mcp-knowledge-server": {
echo       "command": "java",
echo       "args": [
echo         "-cp", 
echo         "%CD%\\target\\mcp-knowledge-server-1.0.0.jar",
echo         "com.korshak.mcpserver.stdin.McpStdinServerApplication",
echo         "--spring.profiles.active=stdin"
echo       ],
echo       "cwd": "%CD%"
echo     }
echo   }
echo }
) > "%CONFIG_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo ✅ Configuration written successfully!
    echo.
    echo File created at: %CONFIG_FILE%
    echo.
    echo Configuration contents:
    echo ------------------------
    type "%CONFIG_FILE%"
    echo.
    echo Step 5: Restart Claude Desktop
    echo ------------------------------
    echo ⚠️  IMPORTANT: You must restart Claude Desktop completely for changes to take effect.
    echo.
    echo 1. Close Claude Desktop
    echo 2. Restart the application
    echo 3. Test with: "What files do you have access to?"
    echo.
    echo ✅ Setup complete! Your MCP Knowledge Server should now be available in Claude Desktop.
) else (
    echo ❌ Failed to write configuration file.
    echo You may need to run this script as administrator.
)

echo.
pause
