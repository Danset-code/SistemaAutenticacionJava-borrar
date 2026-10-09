package com.monitoreo.controllers;

import com.monitoreo.dto.SensorRequest;
import com.monitoreo.models.Sensor;
import com.monitoreo.models.Usuario;
import com.monitoreo.services.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/sensores")
public class SensorController {
    private final SensorService service;
    public SensorController(SensorService service) { this.service=service; }
    @GetMapping public ResponseEntity<?> listar(@RequestAttribute("currentUser") Usuario owner) { return ResponseEntity.ok(service.listar(owner)); }
    @GetMapping("/{id}") public ResponseEntity<Sensor> buscar(@PathVariable Long id, @RequestAttribute("currentUser") Usuario owner) { return ResponseEntity.ok(service.buscar(id, owner)); }
    @PostMapping public ResponseEntity<Sensor> crear(@Valid @RequestBody SensorRequest request, @RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.status(201).body(service.crear(request, owner));
    }
    @PutMapping("/{id}") public ResponseEntity<Sensor> actualizar(@PathVariable Long id, @Valid @RequestBody SensorRequest request, @RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.ok(service.actualizar(id, request, owner));
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> eliminar(@PathVariable Long id, @RequestAttribute("currentUser") Usuario owner) {
        service.eliminar(id, owner);
        return ResponseEntity.ok(Map.of("success", true, "message", "Sensor eliminado correctamente"));
    }
}
