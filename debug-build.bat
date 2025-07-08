@echo off
echo Debugging MCP Knowledge Server Build...
echo ==========================================
echo.

echo Current directory contents:
dir /b
echo.

echo Checking target directory:
if exist "target" (
    echo Target directory exists
    dir /b target
    echo.
    echo Looking for JAR files:
    dir /b target\*.jar 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo ❌ No JAR files found in target directory!
        echo This means the Maven build hasn't completed successfully.
        echo.
        echo Please run: mvn clean package -DskipTests
    )
) else (
    echo ❌ Target directory doesn't exist!
    echo Maven build has never been run.
)

echo.
echo Checking Java installation:
java -version
echo.

echo Checking Maven installation:
mvn -version
echo.

echo ==========================================
echo DIAGNOSIS:
echo ==========================================
echo The JAR file 'mcp-knowledge-server-1.0.0.jar' is missing.
echo Claude Desktop cannot start the server without this file.
echo.
echo SOLUTION:
echo 1. Run: mvn clean package -DskipTests
echo 2. Or run: build-enhanced-formats.bat
echo 3. Check target directory for JAR files
echo 4. Then restart Claude Desktop
echo.
pause
