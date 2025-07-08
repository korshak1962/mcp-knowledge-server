# PowerShell script to start MCP Knowledge Server (Web version)
Write-Host "Starting MCP Knowledge Server (Web/WebSocket version)..." -ForegroundColor Green
Write-Host ""

Write-Host "Checking Java version..." -ForegroundColor Yellow
java -version
Write-Host ""

Write-Host "Building the project..." -ForegroundColor Yellow
mvn clean install -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! Starting the WEB server..." -ForegroundColor Green
    Write-Host ""
    Write-Host "Server will be available at:" -ForegroundColor Cyan
    Write-Host "- WebSocket MCP endpoint: ws://localhost:8080/mcp" -ForegroundColor White
    Write-Host "- REST API base: http://localhost:8080/api/knowledge/" -ForegroundColor White
    Write-Host "- Health check: http://localhost:8080/api/knowledge/health" -ForegroundColor White
    Write-Host ""
    Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
    Write-Host ""
    
    java -jar target/mcp-knowledge-server-1.0.0.jar --spring.profiles.active=default
} else {
    Write-Host "Build failed! Please check the error messages above." -ForegroundColor Red
    Read-Host "Press Enter to continue"
}
