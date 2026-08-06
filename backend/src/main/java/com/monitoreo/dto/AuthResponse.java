package com.monitoreo.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private Long usuarioId;
    private String nombre;
    private String correo;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, Long usuarioId, String nombre, String correo) {
        this.success = success;
        this.message = message;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
}
