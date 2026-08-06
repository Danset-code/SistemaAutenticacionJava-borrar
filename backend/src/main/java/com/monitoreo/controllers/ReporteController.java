package com.monitoreo.controllers;

import com.monitoreo.models.Lectura;
import com.monitoreo.services.LecturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final LecturaService service;

    public ReporteController(LecturaService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> consultar(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay().minusNanos(1);

        return ResponseEntity.ok(service.reporte(sensorId, inicio, fin));
    }
}
