@echo off
cd /d "D:\mcp-knowledge-server"
echo Building with JSON-RPC fix...
mvn clean compile package -DskipTests
echo Build complete - check output above
