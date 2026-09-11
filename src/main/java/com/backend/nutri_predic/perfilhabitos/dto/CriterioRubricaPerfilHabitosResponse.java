package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.CriterioRubricaPerfilHabitos;
import java.math.BigDecimal;

public record CriterioRubricaPerfilHabitosResponse(
        Long id,
        Long dimensionId,
        String codigoDimension,
        String codigo,
        String nombre,
        String tipoEvaluacion,
        String componente,
        String fuenteDatos,
        BigDecimal puntosMaximos,
        String parametrosJson,
        String adherencia7dJson,
        String fuente,
        String organismoAutor,
        String versionAnio,
        String referencia,
        String tipoFuente,
        Integer orden,
        boolean activo) {
    public static CriterioRubricaPerfilHabitosResponse from(CriterioRubricaPerfilHabitos c) {
        return new CriterioRubricaPerfilHabitosResponse(
                c.getId(),
                c.getDimension().getId(),
                c.getDimension().getCodigo().name(),
                c.getCodigo(),
                c.getNombre(),
                c.getTipoEvaluacion().name(),
                c.getComponente(),
                c.getFuenteDatos(),
                c.getPuntosMaximos(),
                c.getParametrosJson(),
                c.getAdherencia7dJson(),
                c.getFuente(),
                c.getOrganismoAutor(),
                c.getVersionAnio(),
                c.getReferencia(),
                c.getTipoFuente().name(),
                c.getOrden(),
                c.isActivo());
    }
}
