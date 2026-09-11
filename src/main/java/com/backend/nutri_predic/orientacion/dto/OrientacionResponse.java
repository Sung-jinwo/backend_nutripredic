package com.backend.nutri_predic.orientacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrientacionResponse(
        boolean personalizadaDisponible,
        String mensaje,
        Evaluacion evaluacion,
        List<Comparacion> comparaciones,
        List<Prioridad> prioridades,
        List<Recomendacion> recomendaciones) {

    public record Evaluacion(
            Long prediccionId,
            LocalDate fechaEvaluada,
            String clasificacion,
            BigDecimal probAdecuado,
            BigDecimal probMejorable,
            BigDecimal probCritico,
            String modelVersion) {}

    public record Comparacion(
            String codigo,
            String nombre,
            String unidad,
            BigDecimal meta,
            BigDecimal consumido,
            BigDecimal porcentaje,
            String estado) {}

    public record Prioridad(
            String codigo,
            String titulo,
            String descripcion,
            BigDecimal desviacionPorcentual) {}

    public record Recomendacion(
            String codigo,
            String titulo,
            String descripcion,
            String basadaEn) {}

    public static OrientacionResponse sinEvaluacion() {
        return new OrientacionResponse(false,
                "Completa al menos un día de registro de alimentos y agua para recibir orientación nutricional personalizada.",
                null, List.of(), List.of(), List.of());
    }
}
