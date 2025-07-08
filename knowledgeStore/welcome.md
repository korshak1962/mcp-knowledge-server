# Welcome to the Knowledge Store

This is a sample markdown file in your knowledge store. The MCP Knowledge Server can read and process various file types including:

- Text files (.txt)
- Markdown files (.md)
- PDF files (.pdf)
- Images (.jpg, .png, .gif)
- And many other formats using Apache Tika

## Features

The server provides the following capabilities:
- List all files in the knowledge store
- Read file contents
- Search for files containing specific text
- Get file metadata
- Write new files

## Usage

You can interact with the knowledge store through:
1. MCP protocol via WebSocket (ws://localhost:8080/mcp)
2. REST API endpoints (/api/knowledge/*)

## Example

This file can be read using the `read_file` tool with filename "welcome.md".
