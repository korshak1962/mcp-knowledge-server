# PowerShell script to start MCP Knowledge Server (Stdin version)
Write-Host "Starting MCP Knowledge Server (stdin/stdout version for Claude Desktop)..." -ForegroundColor Green
Write-Host ""

Write-Host "Checking Java version..." -ForegroundColor Yellow
java -version
Write-Host ""

Write-Host "Building the project..." -ForegroundColor Yellow
mvn clean install -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! Starting the STDIN server..." -ForegroundColor Green
    Write-Host ""
    Write-Host "This server communicates via stdin/stdout for Claude Desktop" -ForegroundColor Cyan
    Write-Host "Add this to your Claude Desktop MCP configuration:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host '  "mcp-knowledge-server": {' -ForegroundColor White
    Write-Host '    "command": "java",' -ForegroundColor White
    Write-Host '    "args": ["-cp", "target/mcp-knowledge-server-1.0.0.jar", "com.korshak.mcpserver.stdin.McpStdinServerApplication", "--spring.profiles.active=stdin"],' -ForegroundColor White
    Write-Host "    `"cwd`": `"$PWD`"" -ForegroundColor White
    Write-Host '  }' -ForegroundColor White
    Write-Host ""
    Write-Host "Starting server now..." -ForegroundColor Green
    Write-Host ""
    
    java -cp target/mcp-knowledge-server-1.0.0.jar com.korshak.mcpserver.stdin.McpStdinServerApplication --spring.profiles.active=stdin
} else {
    Write-Host "Build failed! Please check the error messages above." -ForegroundColor Red
    Read-Host "Press Enter to continue"
}
