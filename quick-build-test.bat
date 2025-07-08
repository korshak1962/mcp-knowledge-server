@echo off
echo Quick build test - fixed Spring Boot configuration conflicts...
echo.

mvn clean compile package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ SUCCESS! Both versions built without conflicts
    echo.
    echo Generated JARs:
    dir /b target\*.jar
    echo.
    echo Package structure fixed:
    echo   - Web version: com.korshak.mcpserver.McpKnowledgeServerApplication
    echo   - Stdin version: com.korshak.mcpserver.stdin.McpStdinServerApplication
    echo.
    echo Ready to run!
) else (
    echo.
    echo ❌ Build failed - check errors above
)

pause
