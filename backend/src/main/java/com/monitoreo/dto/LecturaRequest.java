package com.monitoreo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class LecturaRequest {
    private Long sensorId;
    private String deviceId;
    private String canal;
    private String tipo;
    private String unidad;

    @NotNull(message = "El valor es obligatorio")
    private Double valor;

    private LocalDateTime fecha;

    public Long getSensorId() { return sensorId; }
    public String getDeviceId() { return deviceId; }
    public String getCanal() { return canal; }
    public String getTipo() { return tipo; }
    public String getUnidad() { return unidad; }
    public Double getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }

    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setCanal(String canal) { this.canal = canal; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public void setValor(Double valor) { this.valor = valor; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
