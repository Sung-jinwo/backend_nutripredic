package com.backend.nutri_predic.prediccionmodelo.service;

import java.time.Instant;
import java.util.function.Consumer;

public record TrazabilidadInferencia(
        Consumer<Instant> variablesPreparadas,
        Consumer<Instant> modeloSolicitado,
        Consumer<Instant> modeloRespondio) {
    public static TrazabilidadInferencia sinRegistro() {
        Consumer<Instant> ignorar = instante -> {};
        return new TrazabilidadInferencia(ignorar, ignorar, ignorar);
    }
}
