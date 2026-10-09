package com.monitoreo.controllers;

import com.monitoreo.dto.CodigoVinculacionResponse;
import com.monitoreo.dto.DispositivoVinculadoResponse;
import com.monitoreo.dto.VincularDispositivoRequest;
import com.monitoreo.models.Usuario;
import com.monitoreo.services.DispositivoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {
    private final DispositivoService service;
    public DispositivoController(DispositivoService service) { this.service=service; }

    @GetMapping public ResponseEntity<?> listar(@RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.ok(service.listar(owner));
    }

    @PostMapping("/codigo-vinculacion") public ResponseEntity<CodigoVinculacionResponse> crearCodigo(@RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.status(201).body(service.crearCodigo(owner));
    }

    /** Endpoint público limitado a canjear un código aleatorio de un solo uso generado por una sesión autenticada. */
    @PostMapping("/vincular") public ResponseEntity<DispositivoVinculadoResponse> vincular(@Valid @RequestBody VincularDispositivoRequest request) {
        return ResponseEntity.ok(service.vincular(request));
    }
}
