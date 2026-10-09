package com.monitoreo.controllers;

import com.monitoreo.dto.GraficoRequest;
import com.monitoreo.models.Grafico;
import com.monitoreo.models.Usuario;
import com.monitoreo.services.GraficoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/graficos")
public class GraficoController {
    private final GraficoService service;
    public GraficoController(GraficoService service) { this.service=service; }
    @GetMapping public ResponseEntity<?> listar(@RequestParam(defaultValue="false") boolean activos, @RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.ok(activos ? service.listarActivos(owner) : service.listar(owner));
    }
    @GetMapping("/{id}") public ResponseEntity<Grafico> buscar(@PathVariable Long id, @RequestAttribute("currentUser") Usuario owner) { return ResponseEntity.ok(service.buscar(id, owner)); }
    @PostMapping public ResponseEntity<Grafico> crear(@Valid @RequestBody GraficoRequest request, @RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.status(201).body(service.crear(request, owner));
    }
    @PutMapping("/{id}") public ResponseEntity<Grafico> actualizar(@PathVariable Long id, @Valid @RequestBody GraficoRequest request, @RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.ok(service.actualizar(id, request, owner));
    }
    @PatchMapping("/{id}/activo") public ResponseEntity<Grafico> cambiarActivo(@PathVariable Long id, @RequestParam Boolean activo, @RequestAttribute("currentUser") Usuario owner) {
        return ResponseEntity.ok(service.cambiarActivo(id, activo, owner));
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> eliminar(@PathVariable Long id, @RequestAttribute("currentUser") Usuario owner) {
        service.eliminar(id, owner);
        return ResponseEntity.ok(Map.of("success", true, "message", "Gráfico eliminado correctamente"));
    }
}
