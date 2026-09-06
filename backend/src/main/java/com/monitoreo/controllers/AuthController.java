package com.monitoreo.controllers;

import com.monitoreo.dto.AuthResponse;
import com.monitoreo.dto.LoginRequest;
import com.monitoreo.dto.RegisterRequest;
import com.monitoreo.dto.ForgotPasswordRequest;
import com.monitoreo.dto.ResetPasswordRequest;
import java.util.Map;
import com.monitoreo.services.PasswordResetService;
import com.monitoreo.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final PasswordResetService passwordResetService;
    
    public AuthController(
        AuthService service,
        PasswordResetService passwordResetService
    ) {
        this.service = service;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.iniciarSesion(request));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        String token = passwordResetService.solicitarRecuperacion(
                request.getCorreo()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Solicitud de recuperación creada correctamente",
                        "token", token
                )
        );
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        passwordResetService.cambiarPassword(
                request.getToken(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Contraseña actualizada correctamente"
                )
        );
    }
}
