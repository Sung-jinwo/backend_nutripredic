package com.backend.nutri_predic.indicador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Indicador TPP oficial calculado desde EventoAnalisis SOFTWARE_IA")
public record TppIndicatorResponse(
        @Schema(description = "Promedio del proceso completo; null cuando no hay análisis válidos")
                Double promedioTppMs,
        long totalAnalisisValidos,
        long totalAnalisisExcluidos,
        EstadoDisponibilidadTpp estadoDisponibilidad,
        @Schema(nullable = true) String motivoNoDisponible,
        Map<MotivoExclusionTpp, Long> exclusiones) {}
