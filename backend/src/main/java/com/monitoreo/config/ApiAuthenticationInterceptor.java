package com.monitoreo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoreo.models.Dispositivo;
import com.monitoreo.models.Usuario;
import com.monitoreo.services.DispositivoService;
import com.monitoreo.services.UsuarioSesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Map;

@Component
public class ApiAuthenticationInterceptor implements HandlerInterceptor {
    private final UsuarioSesionService sesiones;
    private final DispositivoService dispositivos;
    private final ObjectMapper mapper;
    public ApiAuthenticationInterceptor(UsuarioSesionService sesiones, DispositivoService dispositivos, ObjectMapper mapper) {
        this.sesiones=sesiones; this.dispositivos=dispositivos; this.mapper=mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path=request.getRequestURI().substring(request.getContextPath().length());
        String method=request.getMethod().toUpperCase();

        // Único endpoint público de dispositivos: canje del código de emparejamiento de un solo uso.
        if ("/api/dispositivos/vincular".equals(path) && "POST".equals(method)) return true;

        boolean protectedPath = path.startsWith("/api/sensores") || path.startsWith("/api/graficos")
                || path.startsWith("/api/reportes") || path.startsWith("/api/auth/usuarios/")
                || path.startsWith("/api/dispositivos") || path.startsWith("/api/lecturas");
        if (!protectedPath) return true;

        String token = bearer(request.getHeader("Authorization"));
        Usuario usuario = sesiones.buscarUsuario(token);
        if (usuario != null) {
            if (path.startsWith("/api/auth/usuarios/")) {
                String tail=path.substring("/api/auth/usuarios/".length());
                try {
                    long requestedId=Long.parseLong(tail);
                    if (!usuario.getId().equals(requestedId)) return reject(response, HttpServletResponse.SC_FORBIDDEN, "No puedes acceder a otro usuario");
                } catch (NumberFormatException e) {
                    return reject(response, HttpServletResponse.SC_BAD_REQUEST, "Identificador de usuario inválido");
                }
            }
            request.setAttribute("currentUser", usuario);
            // For normal browser/API calls, the account comes from the bearer token.
            if ("/api/lecturas".equals(path) && "POST".equals(method)) return true;
            return true;
        }

        // The microcontroller can post readings only with its device-specific key.
        if ("/api/lecturas".equals(path) && "POST".equals(method)) {
            Dispositivo dispositivo=dispositivos.validarClave(request.getHeader("X-Device-Key"));
            if (dispositivo != null) {
                request.setAttribute("currentDevice", dispositivo);
                request.setAttribute("currentUser", dispositivo.getUsuario());
                return true;
            }
        }
        return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Sesión no válida o credenciales de dispositivo ausentes");
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(Map.of("success", false, "message", message, "status", status)));
        return false;
    }
    private String bearer(String value) {
        if (value == null || !value.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        return value.substring(7).trim();
    }
}
