package com.monitoreo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "graficos")
public class Grafico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String tipo;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private Boolean activo = true;

    public Grafico() {}

    public Grafico(String nombre, String tipo, Sensor sensor, Boolean activo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.sensor = sensor;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public Sensor getSensor() { return sensor; }
    public Boolean getActivo() { return activo; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSensor(Sensor sensor) { this.sensor = sensor; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
