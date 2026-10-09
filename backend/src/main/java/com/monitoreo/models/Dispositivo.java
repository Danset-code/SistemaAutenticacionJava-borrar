package com.monitoreo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispositivos", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dispositivo_propietario_alias", columnNames = {"usuario_id", "alias"})
})
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hardware_id", unique = true, length = 80)
    private String hardwareId;

    @Column(nullable = false, length = 30)
    private String alias;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Column(name = "codigo_vinculacion", unique = true, length = 64)
    private String codigoVinculacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Dispositivo() {}
    public Long getId() { return id; }
    public String getHardwareId() { return hardwareId; }
    public String getAlias() { return alias; }
    @JsonIgnore public String getApiKey() { return apiKey; }
    @JsonIgnore public String getCodigoVinculacion() { return codigoVinculacion; }
    @JsonIgnore public Usuario getUsuario() { return usuario; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setId(Long id) { this.id = id; }
    public void setHardwareId(String hardwareId) { this.hardwareId = hardwareId; }
    public void setAlias(String alias) { this.alias = alias; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public void setCodigoVinculacion(String codigoVinculacion) { this.codigoVinculacion = codigoVinculacion; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
