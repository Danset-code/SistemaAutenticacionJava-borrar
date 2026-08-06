package com.monitoreo.controllers;

import com.monitoreo.dto.GraficoRequest;
import com.monitoreo.models.Grafico;
import com.monitoreo.services.GraficoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/graficos")
public class GraficoController {
    private final GraficoService service;

    public GraficoController(GraficoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "false") boolean activos) {
        return ResponseEntity.ok(activos ? service.listarActivos() : service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grafico> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscar(id)); }

    @PostMapping
    public ResponseEntity<Grafico> crear(@Valid @RequestBody GraficoRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grafico> actualizar(@PathVariable Long id, @Valid @RequestBody GraficoRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<Grafico> cambiarActivo(@PathVariable Long id, @RequestParam Boolean activo) {
        return ResponseEntity.ok(service.cambiarActivo(id, activo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Gráfico eliminado correctamente"));
    }
}
