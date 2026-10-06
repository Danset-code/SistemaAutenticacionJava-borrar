package com.monitoreo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sensor_device_channel", columnNames = {"device_id", "canal"})
})
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 20)
    private String unidad;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "device_id", length = 80)
    private String deviceId;

    @Column(length = 50)
    private String canal;

    @Column(nullable = false)
    private Double valorActual = 0.0;

    private LocalDateTime ultimaLectura;

    public Sensor() {}

    public Sensor(String nombre, String tipo, String unidad, String estado, Double valorActual) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.unidad = unidad;
        this.estado = estado;
        this.valorActual = valorActual;
        this.ultimaLectura = null;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getUnidad() { return unidad; }
    public String getEstado() { return estado; }
    public Double getValorActual() { return valorActual; }
    public LocalDateTime getUltimaLectura() { return ultimaLectura; }
    public String getDeviceId() { return deviceId; }
    public String getCanal() { return canal; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setValorActual(Double valorActual) { this.valorActual = valorActual; }
    public void setUltimaLectura(LocalDateTime ultimaLectura) { this.ultimaLectura = ultimaLectura; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setCanal(String canal) { this.canal = canal; }
}
