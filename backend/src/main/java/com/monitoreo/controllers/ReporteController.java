package com.monitoreo.controllers;

import com.monitoreo.services.LecturaService;
import com.monitoreo.services.ExcelReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final LecturaService service;
    private final ExcelReportService excelReportService;

    public ReporteController(LecturaService service, ExcelReportService excelReportService) {
        this.service = service;
        this.excelReportService = excelReportService;
    }

    /**
     * Consulta de reportes para la interfaz.
     * Esta consulta mantiene el límite de registros definido
     * para evitar cargar demasiada información en el navegador.
     */
    @GetMapping
    public ResponseEntity<?> consultar(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDateTime inicio =
                desde == null ? null : desde.atStartOfDay();

        LocalDateTime fin =
                hasta == null
                        ? null
                        : hasta.plusDays(1)
                               .atStartOfDay()
                               .minusNanos(1);

        return ResponseEntity.ok(
                service.reporte(sensorId, inicio, fin)
        );
    }

    /**
     * Consulta completa para exportación.
     * NO utiliza el límite de 50 registros.
     */
    @GetMapping("/exportar")
    public ResponseEntity<?> exportar(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        LocalDateTime inicio =
                desde == null ? null : desde.atStartOfDay();

        LocalDateTime fin =
                hasta == null
                        ? null
                        : hasta.plusDays(1)
                               .atStartOfDay()
                               .minusNanos(1);

        byte[] excel = excelReportService.generar(
                service.reporteCompleto(sensorId, inicio, fin)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_sensores.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .contentLength(excel.length)
                .body(excel);
    }
}
