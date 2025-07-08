@echo off
echo ✅ Duplicate directories removed!
echo.
echo Building clean project...
echo.

mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ BUILD SUCCESSFUL!
    echo.
    echo Testing web version:
    timeout /t 2 >nul
    start /min java -jar target/mcp-knowledge-server-1.0.0.jar
    timeout /t 5 >nul
    
    echo Testing health endpoint...
    curl -s http://localhost:8080/api/knowledge/health
    echo.
    echo.
    
    echo Stopping web server...
    taskkill /f /im java.exe >nul 2>&1
    
    echo.
    echo ✅ Now test stdin version:
    echo java -jar target/mcp-knowledge-server-1.0.0-stdin.jar --spring.profiles.active=stdin
    echo.
    echo ✅ Update Claude Desktop config to use:
    echo target/mcp-knowledge-server-1.0.0-stdin.jar
    echo.
) else (
    echo ❌ Build failed!
)

pause
