package com.autenticacion.services;

import com.autenticacion.dto.AuthResponse;
import com.autenticacion.dto.LoginRequest;
import com.autenticacion.dto.RegisterRequest;
import com.autenticacion.models.Usuario;
import com.autenticacion.repositories.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public AuthResponse registrar(RegisterRequest request) {

        String correo = request.getCorreo()
                .trim()
                .toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            return new AuthResponse(
                    false,
                    "El correo ya está registrado"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(
                request.getNombre().trim()
        );

        usuario.setCorreo(correo);

        usuario.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Usuario guardado =
                usuarioRepository.save(usuario);

        return new AuthResponse(
                true,
                "Usuario registrado correctamente",
                guardado.getId(),
                guardado.getNombre(),
                guardado.getCorreo()
        );
    }

    public AuthResponse iniciarSesion(
            LoginRequest request
    ) {

        String correo = request.getCorreo()
                .trim()
                .toLowerCase();

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(correo)
                        .orElse(null);

        if (usuario == null) {
            return new AuthResponse(
                    false,
                    "Correo o contraseña incorrectos"
            );
        }

        boolean passwordCorrecta =
                passwordEncoder.matches(
                        request.getPassword(),
                        usuario.getPassword()
                );

        if (!passwordCorrecta) {
            return new AuthResponse(
                    false,
                    "Correo o contraseña incorrectos"
            );
        }

        return new AuthResponse(
                true,
                "Inicio de sesión exitoso",
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo()
        );
    }
}
