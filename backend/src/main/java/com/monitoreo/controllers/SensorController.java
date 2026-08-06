package com.monitoreo.controllers;

import com.monitoreo.dto.SensorRequest;
import com.monitoreo.models.Sensor;
import com.monitoreo.services.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sensores")
public class SensorController {
    private final SensorService service;

    public SensorController(SensorService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> listar() { return ResponseEntity.ok(service.listar()); }

    @GetMapping("/{id}")
    public ResponseEntity<Sensor> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscar(id)); }

    @PostMapping
    public ResponseEntity<Sensor> crear(@Valid @RequestBody SensorRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sensor> actualizar(@PathVariable Long id, @Valid @RequestBody SensorRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Sensor> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Sensor eliminado correctamente"));
    }
}
