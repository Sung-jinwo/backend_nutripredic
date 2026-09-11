package com.backend.nutri_predic.ml.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MlPredictResponse(
        String clasificacion,
        MlProbabilidadesResponse probabilidades,
        String modelVersion,
        String schemaVersion,
        BigDecimal inferenceMs,
        Instant inferredAt,
        String metasEstado,
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal aguaMl,
        String formulaNutricionalVersion,
        String fuenteFormulaNutricional,
        String motivoMetasNoDisponibles) {

    public MlPredictResponse(
            String clasificacion, MlProbabilidadesResponse probabilidades, String modelVersion,
            String schemaVersion, BigDecimal inferenceMs, Instant inferredAt) {
        this(clasificacion, probabilidades, modelVersion, schemaVersion, inferenceMs, inferredAt,
                null, null, null, null, null, null, null, null, null);
    }

    public MlPredictResponse(
            String clasificacion,
            MlProbabilidadesResponse probabilidades,
            String modelVersion,
            String schemaVersion) {
        this(clasificacion, probabilidades, modelVersion, schemaVersion, null, null,
                null, null, null, null, null, null, null, null, null);
    }
}
