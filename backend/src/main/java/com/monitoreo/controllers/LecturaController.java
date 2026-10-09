package com.monitoreo.controllers;

import com.monitoreo.dto.LecturaRequest;
import com.monitoreo.models.Dispositivo;
import com.monitoreo.models.Lectura;
import com.monitoreo.models.Usuario;
import com.monitoreo.services.LecturaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/lecturas")
public class LecturaController {
    private final LecturaService service;
    public LecturaController(LecturaService service) { this.service=service; }
    @GetMapping public ResponseEntity<?> listar(@RequestAttribute("currentUser") Usuario owner) { return ResponseEntity.ok(service.listar(owner)); }
    @GetMapping("/{id}") public ResponseEntity<Lectura> buscar(@PathVariable Long id, @RequestAttribute("currentUser") Usuario owner) { return ResponseEntity.ok(service.buscar(id, owner)); }
    @GetMapping("/sensor/{sensorId}") public ResponseEntity<?> porSensor(@PathVariable Long sensorId, @RequestAttribute("currentUser") Usuario owner) { return ResponseEntity.ok(service.porSensor(sensorId, owner)); }
    @PostMapping public ResponseEntity<Lectura> crear(@Valid @RequestBody LecturaRequest request,
            @RequestAttribute("currentUser") Usuario owner,
            @RequestAttribute(value="currentDevice", required=false) Dispositivo device) {
        return ResponseEntity.status(201).body(service.crear(request, owner, device));
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> eliminar(@PathVariable Long id, @RequestAttribute("currentUser") Usuario owner) {
        service.eliminar(id, owner);
        return ResponseEntity.ok(Map.of("success", true, "message", "Lectura eliminada correctamente"));
    }
}
