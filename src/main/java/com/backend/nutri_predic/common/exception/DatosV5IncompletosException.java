package com.backend.nutri_predic.common.exception;

import java.util.List;

/** Evita una llamada al modelo cuando la ventana no puede formar las 21 X V5. */
public class DatosV5IncompletosException extends BusinessException {
    private final List<String> datosFaltantes;

    public DatosV5IncompletosException(List<String> datosFaltantes) {
        super("Faltan datos requeridos para el análisis V5");
        this.datosFaltantes = List.copyOf(datosFaltantes);
    }

    public List<String> getDatosFaltantes() {
        return datosFaltantes;
    }
}
