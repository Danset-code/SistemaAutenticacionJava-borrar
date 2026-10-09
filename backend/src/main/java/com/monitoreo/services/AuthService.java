package com.monitoreo.services;

import com.monitoreo.dto.AuthResponse;
import com.monitoreo.dto.LoginRequest;
import com.monitoreo.dto.RegisterRequest;
import com.monitoreo.dto.UsuarioUpdateRequest;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.DispositivoRepository;
import com.monitoreo.repositories.GraficoRepository;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.PasswordResetTokenRepository;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.repositories.UsuarioRepository;
import com.monitoreo.repositories.UsuarioSesionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UsuarioSesionService sesiones;
    private final SensorRepository sensorRepository;
    private final GraficoRepository graficoRepository;
    private final LecturaRepository lecturaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final UsuarioSesionRepository sesionRepository;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       UsuarioSesionService sesiones, SensorRepository sensorRepository,
                       GraficoRepository graficoRepository, LecturaRepository lecturaRepository,
                       DispositivoRepository dispositivoRepository, UsuarioSesionRepository sesionRepository) {
        this.usuarioRepository=usuarioRepository; this.passwordEncoder=passwordEncoder;
        this.passwordResetTokenRepository=passwordResetTokenRepository; this.sesiones=sesiones;
        this.sensorRepository=sensorRepository; this.graficoRepository=graficoRepository;
        this.lecturaRepository=lecturaRepository; this.dispositivoRepository=dispositivoRepository;
        this.sesionRepository=sesionRepository;
    }

    public AuthResponse registrar(RegisterRequest request) {
        String correo=request.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        Usuario usuario=new Usuario(); usuario.setNombre(request.getNombre().trim()); usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); usuario=usuarioRepository.save(usuario);
        return new AuthResponse(true, "Usuario registrado correctamente", usuario.getId(), usuario.getNombre(), usuario.getCorreo());
    }

    public AuthResponse buscarPorId(Long id) {
        Usuario usuario=usuarioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return new AuthResponse(true, "Usuario encontrado", usuario.getId(), usuario.getNombre(), usuario.getCorreo());
    }

    public AuthResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario=usuarioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if ("admin@monitoreo.com".equalsIgnoreCase(usuario.getCorreo()) && request.getPassword()!=null && !request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La contraseña del administrador no puede modificarse");
        }
        String correo=request.getCorreo().trim().toLowerCase();
        usuarioRepository.findByCorreo(correo).ifPresent(otro -> {
            if (!otro.getId().equals(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado por otro usuario");
        });
        usuario.setNombre(request.getNombre().trim()); usuario.setCorreo(correo);
        if (request.getPassword()!=null && !request.getPassword().isBlank()) usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario=usuarioRepository.save(usuario);
        return new AuthResponse(true, "Usuario actualizado correctamente", usuario.getId(), usuario.getNombre(), usuario.getCorreo());
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario=usuarioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if ("admin@monitoreo.com".equalsIgnoreCase(usuario.getCorreo())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario administrador no puede eliminarse");
        var sensores=sensorRepository.findAllByUsuario_IdOrderByIdAsc(usuario.getId());
        for (var sensor : sensores) {
            graficoRepository.deleteBySensorId(sensor.getId());
            lecturaRepository.deleteBySensorId(sensor.getId());
        }
        sensorRepository.deleteAll(sensores);
        dispositivoRepository.deleteByUsuario_Id(usuario.getId());
        sesionRepository.deleteByUsuario_Id(usuario.getId());
        passwordResetTokenRepository.deleteByUsuario(usuario);
        usuarioRepository.delete(usuario);
    }

    public AuthResponse iniciarSesion(LoginRequest request) {
        String correo=request.getCorreo().trim().toLowerCase();
        Usuario usuario=usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario==null || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        AuthResponse response=new AuthResponse(true, "Inicio de sesión exitoso", usuario.getId(), usuario.getNombre(), usuario.getCorreo());
        response.setAccessToken(sesiones.crear(usuario));
        return response;
    }
}
