package com.autenticacion.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private Long id;
    private String nombre;
    private String correo;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, Long id, String nombre, String correo) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
}
