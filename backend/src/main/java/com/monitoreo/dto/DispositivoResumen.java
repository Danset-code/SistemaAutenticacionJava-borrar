package com.monitoreo.dto;

import com.monitoreo.models.Dispositivo;

public class DispositivoResumen {
    private Long id;
    private String alias;
    private String hardwareId;
    private String estadoVinculacion;
    public DispositivoResumen() {}
    public DispositivoResumen(Dispositivo d) {
        id=d.getId(); alias=d.getAlias(); hardwareId=d.getHardwareId();
        estadoVinculacion=hardwareId == null ? "PENDIENTE" : "VINCULADO";
    }
    public Long getId() { return id; }
    public String getAlias() { return alias; }
    public String getHardwareId() { return hardwareId; }
    public String getEstadoVinculacion() { return estadoVinculacion; }
}
