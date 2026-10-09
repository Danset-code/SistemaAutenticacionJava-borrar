package com.monitoreo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VincularDispositivoRequest {
    @NotBlank(message = "El identificador de hardware es obligatorio")
    @Size(max = 80)
    private String hardwareId;
    @NotBlank(message = "El código de vinculación es obligatorio")
    @Size(max = 64)
    private String pairingCode;
    public String getHardwareId() { return hardwareId; }
    public String getPairingCode() { return pairingCode; }
    public void setHardwareId(String hardwareId) { this.hardwareId = hardwareId; }
    public void setPairingCode(String pairingCode) { this.pairingCode = pairingCode; }
}
