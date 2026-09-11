package com.backend.nutri_predic.consumo.classification;

import com.backend.nutri_predic.consumo.entity.EstadoClasificacionConsumo;
import java.time.LocalDate;
import java.util.List;

public record PcsEvaluationResult(
        Boolean altoConsumo,
        EstadoClasificacionConsumo estadoClasificacion,
        String motivo,
        int criteriosEvaluados,
        int criteriosCumplidos,
        int criteriosNoCalculables,
        Long rubricaId,
        LocalDate fechaCorte,
        List<PcsCriterionTrace> trazas) {
    public PcsEvaluationResult {
        trazas = trazas == null ? List.of() : List.copyOf(trazas);
    }
}
