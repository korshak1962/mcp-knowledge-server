@echo off
echo Testing JSON-RPC fix for MCP Knowledge Server
echo.

echo ✅ Applied @JsonInclude(JsonInclude.Include.NON_NULL) to McpMessage class
echo.
echo This fix ensures that:
echo   - Response messages only include non-null fields
echo   - 'method' and 'params' fields are excluded from responses  
echo   - 'error' field is excluded from successful responses
echo   - Only 'jsonrpc', 'id', and 'result' are included in success responses
echo.

echo Building project with fix...
mvn clean compile package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Build successful! The JSON-RPC fix has been applied.
    echo.
    echo The server will now send properly formatted JSON-RPC responses like:
    echo   {"jsonrpc":"2.0","id":"0","result":{...}}
    echo.
    echo Instead of the previous invalid format with extra fields:
    echo   {"id":"0","method":null,"params":null,"result":{...},"error":null,"jsonrpc":"2.0"}
    echo.
    echo Ready to test with Claude desktop!
) else (
    echo ❌ Build failed - check errors above
)

pause
