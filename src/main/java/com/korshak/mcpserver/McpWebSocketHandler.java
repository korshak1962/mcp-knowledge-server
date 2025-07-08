package com.korshak.mcpserver;

import com.korshak.mcpserver.handler.McpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class McpWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(McpWebSocketHandler.class);
    
    @Autowired
    private McpProtocolHandler protocolHandler;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("WebSocket connection established: {}", session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received message: {}", message.getPayload());
        
        try {
            String response = protocolHandler.handleMessage(message.getPayload());
            session.sendMessage(new TextMessage(response));
            logger.debug("Sent response: {}", response);
        } catch (Exception e) {
            logger.error("Error handling message", e);
            String errorResponse = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal error\"}}";
            session.sendMessage(new TextMessage(errorResponse));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error for session: {}", session.getId(), exception);
    }
}
