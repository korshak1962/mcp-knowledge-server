@echo off
echo Testing compilation fix for PDFBox compatibility...
echo.

echo Cleaning previous build...
mvn clean

echo.
echo Compiling with PDFBox 2.0.29...
mvn compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Compilation successful! 
    echo.
    echo Building full project...
    mvn package -DskipTests
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo 🎉 Build completed successfully!
        echo.
        echo Available JARs:
        dir /b target\*.jar 2>nul
        echo.
        echo ✅ Ready to run:
        echo   - Web server: start-web-server.bat
        echo   - Stdin server: start-stdin-server.bat
        echo   - Test stdin: test-stdin-server.bat
    else (
        echo.
        echo ❌ Package failed - check errors above
    )
) else (
    echo.
    echo ❌ Compilation failed - check errors above
)

echo.
pause
