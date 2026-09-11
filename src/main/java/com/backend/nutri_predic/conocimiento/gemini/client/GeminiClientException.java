package com.backend.nutri_predic.conocimiento.gemini.client;

public class GeminiClientException extends RuntimeException {
    private final String codigoTecnico;
    private final Integer httpStatus;
    private final GeminiResponseDiagnostics diagnostics;
    private final String etapaError;
    private final String tipoExcepcionSeguro;

    public GeminiClientException(String c, Integer s) {
        this(c, s, null, null, null);
    }

    public GeminiClientException(String c, Integer s, GeminiResponseDiagnostics d) {
        this(c, s, d, d == null ? null : d.parseStage(), null);
    }

    public GeminiClientException(
            String c, Integer s, GeminiResponseDiagnostics d, String etapa, String tipo) {
        super(c);
        codigoTecnico = c;
        httpStatus = s;
        diagnostics = d;
        etapaError = etapa;
        tipoExcepcionSeguro = tipo;
    }

    public String getCodigoTecnico() {
        return codigoTecnico;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public GeminiResponseDiagnostics getDiagnostics() {
        return diagnostics;
    }

    public String getEtapaError() {
        return etapaError;
    }

    public String getTipoExcepcionSeguro() {
        return tipoExcepcionSeguro;
    }
}
