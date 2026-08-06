package com.monitoreo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class LecturaRequest {
    @NotNull(message = "El sensor es obligatorio")
    private Long sensorId;

    @NotNull(message = "El valor es obligatorio")
    private Double valor;

    private LocalDateTime fecha;

    public Long getSensorId() { return sensorId; }
    public Double getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setValor(Double valor) { this.valor = valor; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
