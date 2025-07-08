@echo off
echo Building MCP Knowledge Server with com.korshak package...
echo.

echo Cleaning and compiling...
mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful! Now building full project...
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo 🎉 Build completed successfully!
        echo.
        echo Generated JARs:
        dir /b target\*.jar
        echo.
        echo You can now run:
        echo   - Web version: start-web-server.bat
        echo   - Stdin version: start-stdin-server.bat
    else (
        echo.
        echo ❌ Package phase failed. Check errors above.
    )
) else (
    echo.
    echo ❌ Compilation failed. Check errors above.
)

pause
