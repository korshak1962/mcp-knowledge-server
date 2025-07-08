package com.korshak.mcpserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit tests that don't require Spring context loading
 */
class SimpleUnitTests {

    @Test
    void webApplicationClassExists() {
        // Test that the main application class exists and can be instantiated
        assertDoesNotThrow(() -> {
            McpKnowledgeServerApplication.class.getDeclaredConstructor().newInstance();
        });
    }

    @Test
    void stdinApplicationClassExists() {
        // Test that the stdin application class exists
        assertDoesNotThrow(() -> {
            McpStdinServerApplication.class.getDeclaredConstructor().newInstance();
        });
    }

    @Test
    void packageStructureIsCorrect() {
        // Verify the package structure is correct
        assertEquals("com.korshak.mcpserver", McpKnowledgeServerApplication.class.getPackageName());
        assertEquals("com.korshak.mcpserver", McpStdinServerApplication.class.getPackageName());
    }

    @Test
    void mainMethodsExist() {
        // Verify main methods exist
        assertDoesNotThrow(() -> {
            McpKnowledgeServerApplication.class.getMethod("main", String[].class);
        });
        
        assertDoesNotThrow(() -> {
            McpStdinServerApplication.class.getMethod("main", String[].class);
        });
    }
}
