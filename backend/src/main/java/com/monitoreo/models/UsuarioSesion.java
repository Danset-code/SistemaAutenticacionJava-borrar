package com.monitoreo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_usuario")
public class UsuarioSesion {
    @Id
    @Column(name = "token", nullable = false, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    public UsuarioSesion() {}
    public UsuarioSesion(String token, Usuario usuario, LocalDateTime expiraEn) {
        this.token = token; this.usuario = usuario; this.expiraEn = expiraEn;
    }
    public String getToken() { return token; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setToken(String token) { this.token = token; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }
}
