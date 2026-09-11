package com.backend.nutri_predic.consumo.classification;

import java.time.LocalDate;
import java.util.List;

public record PcsFactualSnapshot(
        LocalDate fechaCorte,
        Integer ventanaDias,
        List<PcsObservedValue> valores,
        List<PcsComponentContribution> componentes) {
    public PcsFactualSnapshot {
        valores = valores == null ? List.of() : List.copyOf(valores);
        componentes = componentes == null ? List.of() : List.copyOf(componentes);
    }

    public PcsFactualSnapshot(LocalDate fechaCorte, List<PcsObservedValue> valores) {
        this(fechaCorte, 7, valores, List.of());
    }
}
