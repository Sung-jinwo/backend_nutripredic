package com.backend.nutri_predic.prediccionmodelo.dto;

import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PrediccionModeloHistorialResponse(
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
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal aguaMl,
        String formulaNutricionalVersion,
        String fuenteFormulaNutricional,
        String estado) {
    public static PrediccionModeloHistorialResponse from(PrediccionModelo prediccion) {
        return new PrediccionModeloHistorialResponse(
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
                prediccion.getMetaKcal(),
                prediccion.getMetaProteinaG(),
                prediccion.getMetaCarbohidratosG(),
                prediccion.getMetaGrasasG(),
                prediccion.getMetaAguaMl(),
                prediccion.getFormulaNutricionalVersion(),
                prediccion.getFuenteFormulaNutricional(),
                prediccion.getEstado().name());
    }
}
