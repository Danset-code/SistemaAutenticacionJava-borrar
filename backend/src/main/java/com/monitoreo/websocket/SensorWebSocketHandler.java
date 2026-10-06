package com.monitoreo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SensorWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    private final ObjectMapper mapper;

    public SensorWebSocketHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {

        sessions.add(session);

        System.out.println(
                "WebSocket conectado: " +
                session.getId()
        );
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            org.springframework.web.socket.CloseStatus status) {

        sessions.remove(session);

        System.out.println(
                "WebSocket desconectado: " +
                session.getId()
        );
    }

    public void broadcast(Object payload) {

        try {

            String json =
                    mapper.writeValueAsString(payload);

            TextMessage message =
                    new TextMessage(json);

            for (WebSocketSession session : sessions) {

                if (session.isOpen()) {

                    try {

                        session.sendMessage(message);

                    } catch (Exception e) {

                        sessions.remove(session);

                        System.err.println(
                                "Error enviando WebSocket: " +
                                e.getMessage()
                        );
                    }
                }
            }

        } catch (Exception e) {

            /*
             * NO ocultar el error.
             * Esto permite detectar problemas de
             * serialización del mensaje.
             */

            System.err.println(
                    "ERROR generando mensaje WebSocket: " +
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }
}