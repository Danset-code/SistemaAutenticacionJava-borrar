package com.monitoreo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecturas")
public class Lectura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Lectura() {}

    public Lectura(Sensor sensor, Double valor, LocalDateTime fecha) {
        this.sensor = sensor;
        this.valor = valor;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Sensor getSensor() { return sensor; }
    public Double getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }

    public void setId(Long id) { this.id = id; }
    public void setSensor(Sensor sensor) { this.sensor = sensor; }
    public void setValor(Double valor) { this.valor = valor; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
