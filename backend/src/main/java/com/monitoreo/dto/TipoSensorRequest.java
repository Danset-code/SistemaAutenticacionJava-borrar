package com.monitoreo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class TipoSensorRequest {
    @NotBlank(message = "El nombre del tipo de sensor es obligatorio")
    private String nombre;

    @NotBlank(message = "La unidad es obligatoria")
    private String unidad;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadDisponible = 1;

    private Boolean activo = true;

    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public Integer getCantidadDisponible() { return cantidadDisponible; }
    public Boolean getActivo() { return activo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public void setCantidadDisponible(Integer cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
