package com.korshak.mcpserver.model;

import java.util.List;

public class ToolCallResult {
    private List<Content> content;
    private boolean isError;

    public ToolCallResult() {}

    public ToolCallResult(List<Content> content, boolean isError) {
        this.content = content;
        this.isError = isError;
    }

    public static class Content {
        private String type;
        private String text;

        public Content() {}

        public Content(String type, String text) {
            this.type = type;
            this.text = text;
        }

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    // Getters and setters
    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }
}
