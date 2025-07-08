@echo off
echo ============================================
echo  MCP Knowledge Server - Diagnostics
echo ============================================
echo.

echo 1. Java Version:
java -version 2>&1
echo.

echo 2. Maven Version:
mvn -version 2>&1
echo.

echo 3. Project Structure:
echo Current directory: %CD%
echo.
echo Files in project root:
dir /b *.xml *.bat *.md 2>nul
echo.

echo 4. Target directory:
if exist "target" (
    echo Target directory exists
    echo Contents:
    dir /b target\ 2>nul
    echo.
    echo JAR files:
    dir /b target\*.jar 2>nul
) else (
    echo Target directory does not exist - project needs to be built
)
echo.

echo 5. Source files:
echo Java source files:
dir /b /s src\*.java 2>nul
echo.

echo 6. Configuration files:
echo Properties files:
dir /b /s *.properties 2>nul
echo.

echo 7. Knowledge store:
if exist "knowledgeStore" (
    echo Knowledge store directory exists
    echo Contents:
    dir /b knowledgeStore\ 2>nul
) else (
    echo Knowledge store directory missing
)
echo.

echo 8. Testing Maven compilation:
mvn compile -q -Dorg.slf4j.simpleLogger.defaultLogLevel=error
if %ERRORLEVEL% EQU 0 (
    echo Maven compilation: SUCCESS
) else (
    echo Maven compilation: FAILED
)
echo.

echo ============================================
echo  Diagnosis complete!
echo ============================================
pause
