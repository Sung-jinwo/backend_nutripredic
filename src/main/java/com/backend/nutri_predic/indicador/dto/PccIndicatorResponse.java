package com.backend.nutri_predic.indicador.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicador PCC oficial calculado desde la última evaluación diaria adaptativa válida por cliente")
public record PccIndicatorResponse(
        @Schema(nullable = true) Double porcentajePcc,
        long totalEvaluadosValidos,
        long totalBajoConocimiento,
        EstadoDisponibilidadPcc estadoDisponibilidad,
        @Schema(nullable = true) String motivoNoDisponible) {}
