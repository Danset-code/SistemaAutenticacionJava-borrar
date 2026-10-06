package com.monitoreo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SensorRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotBlank(message = "La unidad es obligatoria")
    private String unidad;

    private String estado;

    @NotNull(message = "El valor inicial es obligatorio")
    private Double valorActual;

    @Size(max = 80)
    private String deviceId;

    @Size(max = 50)
    private String canal;

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getUnidad() { return unidad; }
    public String getEstado() { return estado; }
    public Double getValorActual() { return valorActual; }
    public String getDeviceId() { return deviceId; }
    public String getCanal() { return canal; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setValorActual(Double valorActual) { this.valorActual = valorActual; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setCanal(String canal) { this.canal = canal; }
}
