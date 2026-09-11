package com.backend.nutri_predic.consumo.dto;

import com.backend.nutri_predic.consumo.entity.EvaluacionConsumo;
import java.time.Instant;
import java.time.LocalDate;

public record EvaluacionConsumoResponse(
        Long id,
        Long clienteId,
        Long rubricaId,
        LocalDate fechaInicio,
        LocalDate fechaCorte,
        Integer ventanaDias,
        String momento,
        String estadoClasificacion,
        String motivo,
        String estadoValidez,
        Boolean altoConsumo,
        Instant fechaEvaluacion,
        String advertencias) {
    public static EvaluacionConsumoResponse from(EvaluacionConsumo e) {
        return new EvaluacionConsumoResponse(
                e.getId(),
                e.getCliente().getId(),
                e.getRubrica() == null ? null : e.getRubrica().getId(),
                e.getFechaInicio(),
                e.getFechaCorte(),
                e.getVentanaDias(),
                e.getMomento().name(),
                e.getEstadoClasificacion().name(),
                e.getMotivoClasificacion(),
                e.getEstadoValidez().name(),
                e.getAltoConsumo(),
                e.getFechaEvaluacion(),
                e.getAdvertencias());
    }
}
