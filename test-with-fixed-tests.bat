@echo off
echo ✅ Fixed test files!
echo.
echo Building with tests...
echo.

mvn clean package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ BUILD AND TESTS SUCCESSFUL!
    echo.
    echo Testing stdin version:
    java -jar target/mcp-knowledge-server-1.0.0-stdin.jar --spring.profiles.active=stdin 2>&1
    echo.
    echo ✅ Ready for Claude Desktop!
    echo.
) else (
    echo ❌ Build or tests failed!
)

pause
