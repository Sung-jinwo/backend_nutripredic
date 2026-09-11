package com.backend.nutri_predic.prediccionmodelo.service;

import com.backend.nutri_predic.ml.exception.ModeloMlException;

public class PrediccionModeloFallidaException extends ModeloMlException {
    private final Long prediccionModeloId;

    public PrediccionModeloFallidaException(
            String message, Long prediccionModeloId, Throwable cause) {
        super(message, cause);
        this.prediccionModeloId = prediccionModeloId;
    }

    public Long getPrediccionModeloId() {
        return prediccionModeloId;
    }
}
