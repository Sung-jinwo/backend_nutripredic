package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.ResultadoCriterioPerfilHabitos;
import java.math.BigDecimal;

public record ResultadoCriterioPerfilHabitosResponse(
        Long id,
        Long evaluacionId,
        Long criterioId,
        String codigoCriterio,
        String nombreCriterio,
        String tipoEvaluacion,
        String estado,
        BigDecimal valorObservado,
        BigDecimal puntosObtenidos,
        BigDecimal puntosMaximos,
        String referenciaAplicada,
        String motivoNoCalculable,
        String observacion) {
    public static ResultadoCriterioPerfilHabitosResponse from(ResultadoCriterioPerfilHabitos r) {
        return new ResultadoCriterioPerfilHabitosResponse(
                r.getId(),
                r.getEvaluacion().getId(),
                r.getCriterio().getId(),
                r.getCriterio().getCodigo(),
                r.getCriterio().getNombre(),
                r.getCriterio().getTipoEvaluacion().name(),
                r.getEstado().name(),
                r.getValorObservado(),
                r.getPuntosObtenidos(),
                r.getPuntosMaximos(),
                r.getReferenciaAplicada(),
                r.getMotivoNoCalculable(),
                r.getObservacion());
    }
}
