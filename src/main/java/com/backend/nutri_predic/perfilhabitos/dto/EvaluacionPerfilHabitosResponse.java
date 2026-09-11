package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EvaluacionPerfilHabitosResponse(
        Long id,
        Long clienteId,
        Long rubricaId,
        String rubricaCodigo,
        BigDecimal rubricaVersion,
        Long evaluadorUsuarioId,
        Instant fechaEvaluacion,
        LocalDate fechaCorte,
        BigDecimal puntajeTotal,
        BigDecimal puntajeMaximoCalculable,
        BigDecimal coberturaCalculable,
        String clasificacionReal,
        String estadoValidez,
        String motivoNoValida,
        String observacion,
        Instant creadoEn) {
    public static EvaluacionPerfilHabitosResponse from(EvaluacionPerfilHabitos e) {
        return new EvaluacionPerfilHabitosResponse(
                e.getId(),
                e.getCliente().getId(),
                e.getRubrica().getId(),
                e.getRubrica().getCodigo(),
                e.getRubrica().getVersion(),
                e.getEvaluador().getId(),
                e.getFechaEvaluacion(),
                e.getFechaCorte(),
                e.getPuntajeTotal(),
                e.getPuntajeMaximoCalculable(),
                e.getCoberturaCalculable(),
                e.getClasificacionReal().name(),
                e.getEstadoValidez().name(),
                e.getMotivoNoValida(),
                e.getObservacion(),
                e.getCreadoEn());
    }
}
