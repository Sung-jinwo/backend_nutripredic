package com.backend.nutri_predic.consumo.dto;

import com.backend.nutri_predic.consumo.classification.AmbitoAportePcs;
import com.backend.nutri_predic.consumo.classification.OperadorCriterioPcs;
import com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos;
import java.math.BigDecimal;

public record CriterioConsumoSuplementosResponse(
        Long id,
        Long rubricaId,
        String alcance,
        Long suplementoId,
        String componenteTipo,
        String elementoAplicable,
        String metrica,
        BigDecimal cantidadReferencia,
        BigDecimal cantidadReferenciaHasta,
        AmbitoAportePcs ambitoAporte,
        String unidadReferencia,
        String unidadNormalizada,
        String frecuencia,
        Integer numeroTomas,
        Integer ventanaDias,
        boolean requiereComposicion,
        OperadorCriterioPcs operador,
        String parametros,
        String fuenteReferencia,
        String versionReferencia,
        String observacionMetodologica) {
    public static CriterioConsumoSuplementosResponse from(CriterioConsumoSuplementos criterio) {
        return new CriterioConsumoSuplementosResponse(
                criterio.getId(),
                criterio.getRubrica().getId(),
                criterio.getAlcance(),
                criterio.getSuplemento() == null ? null : criterio.getSuplemento().getId(),
                criterio.getComponenteTipo(),
                criterio.getElementoAplicable(),
                criterio.getMetrica(),
                criterio.getCantidadReferencia(),
                criterio.getCantidadReferenciaHasta(),
                criterio.getAmbitoAporte(),
                criterio.getUnidadReferencia(),
                criterio.getUnidadNormalizada(),
                criterio.getFrecuencia(),
                criterio.getNumeroTomas(),
                criterio.getVentanaDias(),
                criterio.isRequiereComposicion(),
                criterio.getOperador(),
                criterio.getParametros(),
                criterio.getFuenteReferencia(),
                criterio.getVersionReferencia(),
                criterio.getObservacionMetodologica());
    }
}
