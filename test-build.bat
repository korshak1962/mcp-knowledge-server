@echo off
echo Testing Maven build after fixes...
echo.

echo Cleaning and compiling...
mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful! Now building full package...
    echo.
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ✅ Build completed successfully!
        echo.
        echo You can now run:
        echo   start-web-server.bat    (for web version)
        echo   start-stdin-server.bat  (for Claude Desktop)
        echo.
    ) else (
        echo.
        echo ❌ Package build failed!
    )
) else (
    echo.
    echo ❌ Compilation failed! Check errors above.
)

pause
