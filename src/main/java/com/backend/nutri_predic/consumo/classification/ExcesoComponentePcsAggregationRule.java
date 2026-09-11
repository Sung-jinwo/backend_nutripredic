package com.backend.nutri_predic.consumo.classification;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Regla aprobada de exceso por componentes. CUMPLE significa que el valor factual
 * superó la referencia máxima configurada; no significa consumo saludable.
 */
@Component
public class ExcesoComponentePcsAggregationRule implements PcsAggregationRule {
    public static final String CODIGO = "EXCESO_COMPONENTES_V1";

    @Override
    public boolean soporta(String configuracion) {
        return CODIGO.equals(configuracion);
    }

    @Override
    public PcsAggregationResult agregar(String configuracion, List<PcsCriterionTrace> criterios) {
        if (!soporta(configuracion) || criterios == null || criterios.isEmpty()) {
            return PcsAggregationResult.noCalculable("SIN_CRITERIOS_APLICABLES");
        }
        if (criterios.stream().anyMatch(c -> c.resultado() == ResultadoCriterioPcs.CUMPLE)) {
            return new PcsAggregationResult(true, true, null);
        }
        if (criterios.stream().anyMatch(c -> c.resultado() == ResultadoCriterioPcs.NO_CALCULABLE)) {
            return PcsAggregationResult.noCalculable("CRITERIOS_NO_CALCULABLES");
        }
        return new PcsAggregationResult(true, false, null);
    }
}
