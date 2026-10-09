package com.monitoreo.services;

import com.monitoreo.models.PasswordResetToken;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.PasswordResetTokenRepository;
import com.monitoreo.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UsuarioRepository usuarioRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Genera un token para recuperar la contraseña.
     *
     * Por ahora el método devuelve el token para que podamos
     * probar el funcionamiento antes de integrar el correo.
     */
    @Transactional
    public String solicitarRecuperacion(String correo) {

        String correoNormalizado = correo.trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByCorreo(correoNormalizado)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe un usuario con ese correo"
                        )
                );
        
        if ("admin@monitoreo.com".equalsIgnoreCase(usuario.getCorreo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El administrador no puede recuperar ni modificar su contraseña"
            );
        };

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUsuario(usuario);

        // Generar token seguro y aleatorio
        String token = UUID.randomUUID().toString();

        // El token será válido durante 30 minutos
        LocalDateTime expiracion =
                LocalDateTime.now().plusMinutes(30);

        PasswordResetToken passwordResetToken =
                new PasswordResetToken(
                        token,
                        usuario,
                        expiracion
                );

        tokenRepository.save(passwordResetToken);

        return token;
    }

    /**
     * Comprueba que el token exista, no haya sido usado
     * y todavía no haya expirado.
     */
    public PasswordResetToken validarToken(String token) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Token de recuperación no válido"
                                )
                        );

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El token ya fue utilizado"
            );
        }

        if (resetToken.getFechaExpiracion()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El token de recuperación ha expirado"
            );
        }

        return resetToken;
    }

    /**
     * Cambia la contraseña utilizando un token válido.
     */
    @Transactional
    public void cambiarPassword(
            String token,
            String nuevaPassword
    ) {

        PasswordResetToken resetToken =
                validarToken(token);

        Usuario usuario = resetToken.getUsuario();
        
        if ("admin@monitoreo.com".equalsIgnoreCase(usuario.getCorreo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La contraseña del administrador no puede modificarse"
            );
        }

        // Guardar la nueva contraseña utilizando BCrypt
        usuario.setPassword(
                passwordEncoder.encode(nuevaPassword)
        );

        usuarioRepository.save(usuario);

        // El token solamente puede utilizarse una vez
        resetToken.setUsado(true);

        tokenRepository.save(resetToken);
    }
}