package com.backend.nutri_predic.prediccionmodelo.dto;

import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PrediccionModeloResponse(
        Long id,
        LocalDate fechaCorte,
        String momento,
        String clasificacion,
        BigDecimal probAdecuado,
        BigDecimal probMejorable,
        BigDecimal probCritico,
        String modelVersion,
        String schemaVersion,
        Instant fechaPrediccion,
        BigDecimal inferenceMs,
        Instant inferredAt,
        String modelType,
        String trainingDataType,
        Boolean thesisFinalModel,
        Integer featureCount,
        String estado) {
    public PrediccionModeloResponse(
            Long id,
            LocalDate fechaCorte,
            String momento,
            String clasificacion,
            BigDecimal probAdecuado,
            BigDecimal probMejorable,
            BigDecimal probCritico,
            String modelVersion,
            String schemaVersion,
            Instant fechaPrediccion,
            Long inferenceMs,
            String estado) {
        this(
                id,
                fechaCorte,
                momento,
                clasificacion,
                probAdecuado,
                probMejorable,
                probCritico,
                modelVersion,
                schemaVersion,
                fechaPrediccion,
                inferenceMs == null ? null : BigDecimal.valueOf(inferenceMs),
                null,
                null,
                null,
                null,
                null,
                estado);
    }

    public static PrediccionModeloResponse from(PrediccionModelo prediccion) {
        return new PrediccionModeloResponse(
                prediccion.getId(),
                prediccion.getFechaCorte(),
                prediccion.getMomentoEvaluacion().name(),
                prediccion.getClasificacionPredicha() == null
                        ? null
                        : prediccion.getClasificacionPredicha().name(),
                prediccion.getProbAdecuado(),
                prediccion.getProbMejorable(),
                prediccion.getProbCritico(),
                prediccion.getModelVersion(),
                prediccion.getSchemaVersion(),
                prediccion.getFechaPrediccion(),
                prediccion.getInferenceMs(),
                prediccion.getInferredAt(),
                prediccion.getModelType(),
                prediccion.getTrainingDataType(),
                prediccion.getThesisFinalModel(),
                prediccion.getFeatureCount(),
                prediccion.getEstado().name());
    }
}
