package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.RubricaPerfilHabitos;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RubricaPerfilHabitosResponse(
        Long id,
        String codigo,
        BigDecimal version,
        String nombre,
        String descripcion,
        String estado,
        boolean validada,
        Instant vigenteDesde,
        Instant vigenteHasta,
        String validadoPor,
        Instant validadoEn,
        String observacionValidacion,
        Instant creadoEn,
        List<CriterioClasificacionPerfilResponse> criterios,
        List<DimensionRubricaPerfilHabitosResponse> dimensiones,
        List<CriterioRubricaPerfilHabitosResponse> criteriosEvaluacion) {
    public static RubricaPerfilHabitosResponse from(
            RubricaPerfilHabitos r,
            List<CriterioClasificacionPerfilResponse> criterios,
            List<DimensionRubricaPerfilHabitosResponse> dimensiones,
            List<CriterioRubricaPerfilHabitosResponse> criteriosEvaluacion) {
        return new RubricaPerfilHabitosResponse(
                r.getId(),
                r.getCodigo(),
                r.getVersion(),
                r.getNombre(),
                r.getDescripcion(),
                r.getEstado().name(),
                r.getValidadoPor() != null && r.getValidadoEn() != null,
                r.getVigenteDesde(),
                r.getVigenteHasta(),
                r.getValidadoPor(),
                r.getValidadoEn(),
                r.getObservacionValidacion(),
                r.getCreadoEn(),
                criterios,
                dimensiones,
                criteriosEvaluacion);
    }

    public static RubricaPerfilHabitosResponse from(
            RubricaPerfilHabitos r, List<CriterioClasificacionPerfilResponse> criterios) {
        return from(r, criterios, List.of(), List.of());
    }
}
