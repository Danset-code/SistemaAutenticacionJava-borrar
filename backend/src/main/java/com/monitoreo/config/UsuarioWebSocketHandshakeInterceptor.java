package com.monitoreo.config;

import com.monitoreo.models.Usuario;
import com.monitoreo.services.UsuarioSesionService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class UsuarioWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final UsuarioSesionService sesiones;
    public UsuarioWebSocketHandshakeInterceptor(UsuarioSesionService sesiones) { this.sesiones=sesiones; }
    @Override public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String,Object> attributes) {
        String token = queryValue(request.getURI(), "token");
        Usuario usuario = sesiones.buscarUsuario(token);
        if (usuario == null) return false;
        attributes.put("usuarioId", usuario.getId());
        return true;
    }
    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
    private String queryValue(URI uri, String name) {
        String query=uri.getRawQuery(); if (query==null) return null;
        for (String pair: query.split("&")) {
            String[] p=pair.split("=",2);
            if (p.length==2 && name.equals(p[0])) return URLDecoder.decode(p[1], StandardCharsets.UTF_8);
        }
        return null;
    }
}
