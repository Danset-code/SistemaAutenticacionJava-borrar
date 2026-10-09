package com.monitoreo.controllers;

import com.monitoreo.dto.AuthResponse;
import com.monitoreo.dto.ForgotPasswordRequest;
import com.monitoreo.dto.LoginRequest;
import com.monitoreo.dto.RegisterRequest;
import com.monitoreo.dto.ResetPasswordRequest;
import com.monitoreo.dto.UsuarioUpdateRequest;
import com.monitoreo.services.AuthService;
import com.monitoreo.services.PasswordResetService;
import com.monitoreo.services.UsuarioSesionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final PasswordResetService passwordResetService;
    private final UsuarioSesionService sesiones;

    public AuthController(AuthService service, PasswordResetService passwordResetService, UsuarioSesionService sesiones) {
        this.service=service; this.passwordResetService=passwordResetService; this.sesiones=sesiones;
    }
    @PostMapping("/register") public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
    @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.iniciarSesion(request));
    }
    @GetMapping("/usuarios/{id}") public ResponseEntity<AuthResponse> buscarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
    @PutMapping("/usuarios/{id}") public ResponseEntity<AuthResponse> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }
    @DeleteMapping("/usuarios/{id}") public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Usuario eliminado correctamente"));
    }
    @PostMapping("/logout") public ResponseEntity<?> logout(@RequestHeader(value="Authorization", required=false) String authorization) {
        String token=authorization!=null && authorization.regionMatches(true, 0, "Bearer ", 0, 7) ? authorization.substring(7).trim() : null;
        sesiones.cerrar(token);
        return ResponseEntity.ok(Map.of("success", true, "message", "Sesión cerrada correctamente"));
    }
    @PostMapping("/forgot-password") public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token=passwordResetService.solicitarRecuperacion(request.getCorreo());
        return ResponseEntity.ok(Map.of("success", true, "message", "Solicitud de recuperación creada correctamente", "token", token));
    }
    @PostMapping("/reset-password") public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.cambiarPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok(Map.of("success", true, "message", "Contraseña actualizada correctamente"));
    }
}
