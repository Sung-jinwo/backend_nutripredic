package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.ResultadoDimensionPerfilHabitos;
import java.math.BigDecimal;

public record ResultadoDimensionPerfilHabitosResponse(
        Long id,
        Long evaluacionId,
        Long dimensionId,
        String codigoDimension,
        String nombreDimension,
        BigDecimal puntosObtenidos,
        BigDecimal puntosMaximosCalculables,
        BigDecimal coberturaCalculable,
        String criteriosNoCalculables,
        String observacion) {
    public static ResultadoDimensionPerfilHabitosResponse from(ResultadoDimensionPerfilHabitos r) {
        return new ResultadoDimensionPerfilHabitosResponse(
                r.getId(),
                r.getEvaluacion().getId(),
                r.getDimension().getId(),
                r.getDimension().getCodigo().name(),
                r.getDimension().getNombre(),
                r.getPuntosObtenidos(),
                r.getPuntosMaximosCalculables(),
                r.getCoberturaCalculable(),
                r.getCriteriosNoCalculables(),
                r.getObservacion());
    }
}
