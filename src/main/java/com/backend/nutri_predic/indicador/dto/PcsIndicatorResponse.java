package com.backend.nutri_predic.indicador.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicador PCS oficial calculado desde EvaluacionConsumo")
public record PcsIndicatorResponse(
        @Schema(nullable = true) Double porcentajePcs,
        long totalEvaluadosValidos,
        long totalAltoConsumo,
        EstadoDisponibilidadPcs estadoDisponibilidad,
        @Schema(nullable = true) String motivoNoDisponible) {}
