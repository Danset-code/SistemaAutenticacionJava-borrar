
package com.monitoreo.controllers;

import com.monitoreo.models.Lectura;
import com.monitoreo.models.Usuario;
import com.monitoreo.services.ExcelReportService;
import com.monitoreo.services.LecturaService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final LecturaService service;
    private final ExcelReportService excelReportService;

    public ReporteController(
            LecturaService service,
            ExcelReportService excelReportService) {
        this.service = service;
        this.excelReportService = excelReportService;
    }

    /**
     * Consulta los últimos 50 registros que coincidan con
     * los filtros y que pertenezcan al usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<Lectura>> consultar(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,
            @RequestAttribute("currentUser") Usuario owner) {

        validarRangoFechas(desde, hasta);

        LocalDateTime inicio =
                desde == null ? null : desde.atStartOfDay();

        // Límite exclusivo: incluye todo el día seleccionado.
        LocalDateTime finExclusivo =
                hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        return ResponseEntity.ok(
                service.reporte(
                        sensorId,
                        deviceId,
                        inicio,
                        finExclusivo,
                        owner
                )
        );
    }

    /**
     * Exporta todos los registros que coincidan con los filtros.
     * No utiliza los registros que se muestran en la interfaz.
     */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) Long sensorId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta,
            @RequestAttribute("currentUser") Usuario owner) {

        validarRangoFechas(desde, hasta);

        LocalDateTime inicio =
                desde == null ? null : desde.atStartOfDay();

        LocalDateTime finExclusivo =
                hasta == null ? null : hasta.plusDays(1).atStartOfDay();

        List<Lectura> lecturas = service.reporteCompleto(
                sensorId,
                deviceId,
                inicio,
                finExclusivo,
                owner
        );

        byte[] excel = excelReportService.generar(lecturas);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"reporte_sensores.xlsx\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .contentLength(excel.length)
                .body(excel);
    }

    private void validarRangoFechas(
            LocalDate desde,
            LocalDate hasta) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
    }
}