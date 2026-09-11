package com.backend.nutri_predic.consumo.dto;

import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import java.time.Instant;
import java.util.List;

public record RubricaConsumoSuplementosResponse(
        Long id,
        String codigo,
        Integer version,
        String estado,
        Integer ventanaDias,
        Instant vigenteDesde,
        Instant vigenteHasta,
        boolean validada,
        String observacion,
        String fuenteReferencia,
        String validadoPor,
        Instant validadoEn,
        String versionEvaluador,
        String metadataValidacion,
        String reglaGlobal,
        List<CriterioConsumoSuplementosResponse> criterios) {
    public static RubricaConsumoSuplementosResponse from(
            RubricaConsumoSuplementos rubrica, List<CriterioConsumoSuplementosResponse> criterios) {
        return new RubricaConsumoSuplementosResponse(
                rubrica.getId(),
                rubrica.getCodigo(),
                rubrica.getVersion(),
                rubrica.getEstado().name(),
                rubrica.getVentanaDias(),
                rubrica.getVigenteDesde(),
                rubrica.getVigenteHasta(),
                rubrica.isValidada(),
                rubrica.getObservacion(),
                rubrica.getFuenteReferencia(),
                rubrica.getValidadoPor(),
                rubrica.getValidadoEn(),
                rubrica.getVersionEvaluador(),
                rubrica.getMetadataValidacion(),
                rubrica.getReglaGlobal(),
                criterios);
    }
}
