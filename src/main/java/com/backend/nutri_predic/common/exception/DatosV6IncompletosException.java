package com.backend.nutri_predic.common.exception;

import java.util.List;

/** Impide llamar al modelo cuando no puede construirse una observación V6 completa. */
public class DatosV6IncompletosException extends BusinessException {
    private final List<String> datosFaltantes;

    public DatosV6IncompletosException(List<String> datosFaltantes) {
        super("Faltan datos requeridos para el análisis V6");
        this.datosFaltantes = List.copyOf(datosFaltantes);
    }

    public List<String> getDatosFaltantes() {
        return datosFaltantes;
    }
}
