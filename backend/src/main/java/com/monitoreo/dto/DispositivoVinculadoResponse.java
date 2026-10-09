package com.monitoreo.dto;

public class DispositivoVinculadoResponse {
    private boolean success;
    private String alias;
    private String hardwareId;
    private String deviceKey;
    private String message;
    public DispositivoVinculadoResponse() {}
    public DispositivoVinculadoResponse(boolean success, String alias, String hardwareId, String deviceKey, String message) {
        this.success=success; this.alias=alias; this.hardwareId=hardwareId; this.deviceKey=deviceKey; this.message=message;
    }
    public boolean isSuccess() { return success; }
    public String getAlias() { return alias; }
    public String getHardwareId() { return hardwareId; }
    public String getDeviceKey() { return deviceKey; }
    public String getMessage() { return message; }
}
