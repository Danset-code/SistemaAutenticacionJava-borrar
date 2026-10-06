package com.monitoreo.config;

import com.monitoreo.websocket.SensorWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SensorWebSocketHandler handler;

    public WebSocketConfig(SensorWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/sensores")
                .setAllowedOrigins(
                        "http://localhost:8081",
                        "http://127.0.0.1:8081",
                        "https://sistemaautenticacionjava-borrar.onrender.com"
                );
    }
}
