package com.backend.nutri_predic.datasetmodelov5.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PreparacionDatasetModeloV5Response(Resumen resumen, List<Fila> filas) {
    public record Resumen(
            long totalClientes,
            long totalCortesAuditados,
            long clientesConPerfilV5,
            long clientesConAlimentacionCompleta,
            long clientesConSuplementacionCompleta,
            long clientesConHabitosCompletos,
            long filasCon21X,
            long evaluacionesGroundTruthValidas,
            long filasPotencialmenteEntrenables) {}

    public record Fila(
            Long clienteId,
            LocalDate fechaCorte,
            Long evaluacionId,
            int cantidadXNoNull,
            int cantidadXNull,
            double porcentajeCompletitudX,
            boolean alimentacionCompleta,
            boolean suplementacionCompleta,
            boolean habitosCompletos,
            boolean perfilDisponible,
            boolean groundTruthValido,
            boolean smokeTecnico,
            boolean aptoParaEntrenamientoCompleto,
            Map<String, String> motivosXNull) {}
}
