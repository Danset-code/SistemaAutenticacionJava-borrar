package com.monitoreo.dto;

public class CodigoVinculacionResponse {
    private String alias;
    private String pairingCode;
    private String instrucciones;
    public CodigoVinculacionResponse() {}
    public CodigoVinculacionResponse(String alias, String pairingCode, String instrucciones) {
        this.alias = alias; this.pairingCode = pairingCode; this.instrucciones = instrucciones;
    }
    public String getAlias() { return alias; }
    public String getPairingCode() { return pairingCode; }
    public String getInstrucciones() { return instrucciones; }
}
