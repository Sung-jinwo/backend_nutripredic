package com.backend.nutri_predic.prediccionmodelo.dto;

import com.backend.nutri_predic.ml.dto.MlProbabilidadesResponse;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Resultado oficial de una ejecución predictiva V5 instrumentada")
public record AnalisisPredictivoResponse(
        Long prediccionId,
        Long eventoAnalisisId,
        LocalDate fechaCorte,
        String momento,
        @Schema(allowableValues = {"ADECUADO", "MEJORABLE", "CRITICO"}) String clasificacion,
        MlProbabilidadesResponse probabilidades,
        String modelVersion,
        String schemaVersion,
        @Schema(description = "Duración técnica de inferencia; no representa el TPP")
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
        @Schema(allowableValues = {"GENERADO", "REUTILIZADO"}) String origenResultado,
        @Schema(allowableValues = {"VALIDA"}) String estado,
        Instant analisisIniciadoEn,
        Instant resultadoDisponibleEn,
        @Schema(allowableValues = {"GENERADA", "PENDIENTE", "NO_DISPONIBLE"}) String estadoPccIa,
        @Schema(allowableValues = {"ALTO", "NO_ALTO", "NO_DETERMINADA"}) String estadoPcs,
        @Schema(allowableValues = {"PENDIENTE", "COMPLETADO", "FALLIDO"}) String estadoCicloDiario,
        Long procesamientoCicloMs,
        String moduloFalloCiclo,
        String motivoFalloCiclo) {

    public AnalisisPredictivoResponse(
            Long prediccionId, Long eventoAnalisisId, LocalDate fechaCorte, String momento,
            String clasificacion, MlProbabilidadesResponse probabilidades, String modelVersion,
            String schemaVersion, BigDecimal inferenceMs, Instant inferredAt, String origenResultado,
            String estado, Instant analisisIniciadoEn, Instant resultadoDisponibleEn) {
        this(prediccionId, eventoAnalisisId, fechaCorte, momento, clasificacion, probabilidades,
                modelVersion, schemaVersion, inferenceMs, inferredAt, null, null, null, null,
                null, null, null, null, null, null, null, origenResultado, estado,
                analisisIniciadoEn, resultadoDisponibleEn, null, null, null, null, null, null);
    }

    public static AnalisisPredictivoResponse from(
            EventoAnalisis evento, PrediccionModelo prediccion, String estadoPccIa, String estadoPcs) {
        return new AnalisisPredictivoResponse(
                prediccion.getId(),
                evento.getId(),
                prediccion.getFechaCorte(),
                prediccion.getMomentoEvaluacion().name(),
                prediccion.getClasificacionPredicha().name(),
                new MlProbabilidadesResponse(
                        prediccion.getProbAdecuado(),
                        prediccion.getProbMejorable(),
                        prediccion.getProbCritico()),
                prediccion.getModelVersion(),
                prediccion.getSchemaVersion(),
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
                evento.getOrigenResultado().name(),
                evento.getEstadoValidez().name(),
                evento.getAnalisisIniciadoEn(),
                evento.getResultadoDisponibleEn(),
                estadoPccIa,
                estadoPcs,
                evento.getEstadoCicloDiario() == null ? null : evento.getEstadoCicloDiario().name(),
                evento.getProcesamientoCicloMs(),
                evento.getModuloFalloCiclo(),
                evento.getMotivoFalloCiclo());
    }

    /** Compatibilidad para consumidores Java del contrato previo al ciclo posterior. */
    public static AnalisisPredictivoResponse from(EventoAnalisis evento, PrediccionModelo prediccion) {
        return from(evento, prediccion, null, null);
    }
}
