@echo off
echo Building MCP Knowledge Server after JSON-RPC fix...
echo.

echo Cleaning previous build...
mvn clean

echo.
echo Compiling with fix...
mvn compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful! Building JARs...
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo 🎉 Build completed successfully!
        echo.
        echo Generated JARs:
        dir /b target\*.jar | findstr "stdin"
        echo.
        echo ✅ JSON-RPC fix applied and built successfully!
        echo The server should now send proper JSON-RPC responses.
        echo.
        echo Test with: start-stdin-server.bat
    else (
        echo.
        echo ❌ Package phase failed. Check errors above.
    )
) else (
    echo.
    echo ❌ Compilation failed. Check errors above.
)

echo.
echo Press any key to continue...
pause > nul
