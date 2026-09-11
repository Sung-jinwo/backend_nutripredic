package com.backend.nutri_predic.datasetmodelov5.dto;

import java.util.Map;

public record CalidadDatasetModeloV5Response(
        long totalEvaluacionesValidas,
        long totalFilasExportadas,
        long filasCompletas,
        long filasConNull,
        Map<String, Long> distribucionClasificacion,
        Map<String, CalidadFeature> features) {
    public record CalidadFeature(long nullCount, double porcentajeNull) {}
}
