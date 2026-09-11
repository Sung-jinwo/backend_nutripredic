package com.backend.nutri_predic.consumo.classification;

public record PcsAggregationResult(boolean calculable, Boolean altoConsumo, String motivo) {
    public static PcsAggregationResult noCalculable(String motivo) {
        return new PcsAggregationResult(false, null, motivo);
    }
}
