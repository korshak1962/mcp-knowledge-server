@echo off
echo Removing old backup file...
if exist "McpStdinServerApplication.java.old.deleted" (
    del "McpStdinServerApplication.java.old.deleted"
    echo ✅ Old backup file deleted successfully
) else (
    echo ⚠️ Old backup file not found
)

echo.
echo Current project structure:
dir /b src\main\java\com\korshak\mcpserver\*.java
echo.
echo Stdin directory:
dir /b src\main\java\com\korshak\mcpserver\stdin\*.java
echo.
echo ✅ Cleanup complete! Only the active files remain.
pause
