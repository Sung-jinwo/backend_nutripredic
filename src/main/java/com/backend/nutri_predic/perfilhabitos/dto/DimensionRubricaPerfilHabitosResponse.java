package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.DimensionRubricaPerfilHabitos;
import java.math.BigDecimal;

public record DimensionRubricaPerfilHabitosResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal puntajeMinimo,
        BigDecimal puntajeMaximo,
        BigDecimal pesoPorcentual,
        Integer orden) {
    public static DimensionRubricaPerfilHabitosResponse from(
            DimensionRubricaPerfilHabitos dimension) {
        return new DimensionRubricaPerfilHabitosResponse(
                dimension.getId(),
                dimension.getCodigo().name(),
                dimension.getNombre(),
                dimension.getDescripcion(),
                dimension.getPuntajeMinimo(),
                dimension.getPuntajeMaximo(),
                dimension.getPesoPorcentual(),
                dimension.getOrden());
    }
}
