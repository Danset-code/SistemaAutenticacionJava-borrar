package com.monitoreo.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_sensores")
public class TipoSensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String unidad;

    @Column(nullable = false)
    private Integer cantidadDisponible = 1;

    @Column(nullable = false)
    private Boolean activo = true;

    public TipoSensor() {}

    public TipoSensor(String nombre, String unidad, Integer cantidadDisponible) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.cantidadDisponible = cantidadDisponible == null ? 1 : Math.max(0, cantidadDisponible);
        this.activo = true;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public Integer getCantidadDisponible() { return cantidadDisponible; }
    public Boolean getActivo() { return activo; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible == null ? 0 : Math.max(0, cantidadDisponible);
    }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
