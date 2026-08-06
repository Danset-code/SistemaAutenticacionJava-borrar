package com.monitoreo.controllers;

import com.monitoreo.dto.LecturaRequest;
import com.monitoreo.models.Lectura;
import com.monitoreo.services.LecturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lecturas")
public class LecturaController {
    private final LecturaService service;

    public LecturaController(LecturaService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> listar() { return ResponseEntity.ok(service.listar()); }

    @GetMapping("/{id}")
    public ResponseEntity<Lectura> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscar(id)); }

    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<?> porSensor(@PathVariable Long sensorId) { return ResponseEntity.ok(service.porSensor(sensorId)); }

    @PostMapping
    public ResponseEntity<Lectura> crear(@Valid @RequestBody LecturaRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Lectura eliminada correctamente"));
    }
}
