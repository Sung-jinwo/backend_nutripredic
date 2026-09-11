package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.CriterioClasificacionPerfil;
import java.math.BigDecimal;

public record CriterioClasificacionPerfilResponse(
        Long id,
        String clasificacion,
        BigDecimal limiteInferior,
        BigDecimal limiteSuperior,
        boolean incluyeInferior,
        boolean incluyeSuperior,
        Integer orden) {
    public static CriterioClasificacionPerfilResponse from(CriterioClasificacionPerfil c) {
        return new CriterioClasificacionPerfilResponse(
                c.getId(),
                c.getClasificacion().name(),
                c.getLimiteInferior(),
                c.getLimiteSuperior(),
                c.isIncluyeInferior(),
                c.isIncluyeSuperior(),
                c.getOrden());
    }
}
