package com.monitoreo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GraficoRequest {
    @NotBlank(message = "El nombre del gráfico es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El tipo de gráfico es obligatorio")
    private String tipo;

    @NotNull(message = "El sensor es obligatorio")
    private Long sensorId;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public Long getSensorId() { return sensorId; }
    public Boolean getActivo() { return activo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
