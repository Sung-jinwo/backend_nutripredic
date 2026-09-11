package com.backend.nutri_predic.consumo.classification;

import java.math.BigDecimal;

public record PcsCriterionTrace(
        Long criterioId,
        String componente,
        BigDecimal aporteAlimentos,
        BigDecimal aporteSuplementos,
        BigDecimal valorObservadoFinal,
        AmbitoAportePcs ambitoAporte,
        String unidad,
        OperadorCriterioPcs operador,
        BigDecimal valorReferencia,
        BigDecimal valorReferenciaHasta,
        ResultadoCriterioPcs resultado,
        String motivoNoCalculable) {
    public BigDecimal datoObservado() {
        return valorObservadoFinal;
    }
}
