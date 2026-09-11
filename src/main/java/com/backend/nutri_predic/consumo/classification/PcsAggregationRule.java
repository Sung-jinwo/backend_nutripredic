package com.backend.nutri_predic.consumo.classification;

import java.util.List;

/** Agrega resultados de criterios. En PCS, CUMPLE significa evidencia de exceso. */
public interface PcsAggregationRule {
    boolean soporta(String configuracion);

    PcsAggregationResult agregar(String configuracion, List<PcsCriterionTrace> criterios);
}
