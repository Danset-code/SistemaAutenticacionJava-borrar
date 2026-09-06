package com.monitoreo.controllers;

import com.monitoreo.dto.TipoSensorRequest;
import com.monitoreo.models.TipoSensor;
import com.monitoreo.services.TipoSensorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tipos-sensores")
public class TipoSensorController {
    private final TipoSensorService service;

    public TipoSensorController(TipoSensorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "true") boolean activos) {
        return ResponseEntity.ok(service.listar(activos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoSensor> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<TipoSensor> crear(@Valid @RequestBody TipoSensorRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoSensor> actualizar(@PathVariable Long id, @Valid @RequestBody TipoSensorRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<TipoSensor> cambiarActivo(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(service.cambiarActivo(id, activo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Tipo de sensor eliminado del catálogo"));
    }
}
