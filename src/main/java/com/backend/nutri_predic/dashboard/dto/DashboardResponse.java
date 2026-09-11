package com.backend.nutri_predic.dashboard.dto;

import com.backend.nutri_predic.indicador.dto.PccIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.PcsIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.TppIndicatorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dashboard administrativo con indicadores oficiales ya calculados")
public record DashboardResponse(
        long totalClientes,
        long totalPrediccionesV5,
        PccIndicatorResponse pcc,
        PcsIndicatorResponse pcs,
        TppIndicatorResponse tpp,
        @Schema(
                        description =
                                "Dato operativo histórico separado del PCS; null cuando no hay datos",
                        nullable = true)
                Double porcentajeNivelConsumoOperativo,
        @Schema(nullable = true) ModeloActivoResponse modeloActivo) {

    public record ModeloActivoResponse(String version, String schemaVersion) {}
}
