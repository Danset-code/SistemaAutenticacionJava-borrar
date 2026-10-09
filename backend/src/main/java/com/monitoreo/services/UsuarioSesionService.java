package com.monitoreo.services;

import com.monitoreo.models.Usuario;
import com.monitoreo.models.UsuarioSesion;
import com.monitoreo.repositories.UsuarioSesionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioSesionService {
    private static final long DURACION_HORAS = 8;
    private final UsuarioSesionRepository repository;
    public UsuarioSesionService(UsuarioSesionRepository repository) { this.repository = repository; }

    @Transactional
    public String crear(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        repository.save(new UsuarioSesion(token, usuario, LocalDateTime.now().plusHours(DURACION_HORAS)));
        return token;
    }

    @Transactional
    public Usuario buscarUsuario(String token) {
        if (token == null || token.isBlank()) return null;
        UsuarioSesion sesion = repository.findById(token.trim()).orElse(null);
        if (sesion == null) return null;
        if (sesion.getExpiraEn() == null || !sesion.getExpiraEn().isAfter(LocalDateTime.now())) {
            repository.delete(sesion);
            return null;
        }
        return sesion.getUsuario();
    }

    @Transactional
    public void cerrar(String token) {
        if (token != null && !token.isBlank()) repository.deleteById(token.trim());
    }
}
