package com.backend.nutri_predic.prediccionmodelo.evento.dto;

import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import java.time.*;

public record EventoAnalisisResponse(
        Long id,
        Long clienteId,
        LocalDate fechaCorte,
        String momento,
        String procedimientoTipo,
        String procedimientoCodigo,
        Instant inicio,
        Instant fin,
        Instant analisisIniciadoEn,
        Instant variablesPreparadasEn,
        Instant modeloSolicitadoEn,
        Instant modeloRespondioEn,
        Instant resultadoDisponibleEn,
        Long tppTotalMs,
        Long preparacionMs,
        Long esperaMs,
        Long latenciaModeloMs,
        Long postprocesamientoMs,
        Long prediccionModeloId,
        String origenResultado,
        String estadoValidez) {
    static Long d(Instant a, Instant b) {
        return a == null || b == null ? null : Duration.between(a, b).toMillis();
    }

    public static EventoAnalisisResponse from(EventoAnalisis e) {
        return new EventoAnalisisResponse(
                e.getId(),
                e.getCliente().getId(),
                e.getFechaCorte(),
                e.getMomento().name(),
                e.getProcedimiento() == null ? null : e.getProcedimiento().getTipo().name(),
                e.getProcedimiento() == null ? null : e.getProcedimiento().getCodigo(),
                e.getAnalisisIniciadoEn(),
                e.getResultadoDisponibleEn(),
                e.getAnalisisIniciadoEn(),
                e.getVariablesPreparadasEn(),
                e.getModeloSolicitadoEn(),
                e.getModeloRespondioEn(),
                e.getResultadoDisponibleEn(),
                d(e.getAnalisisIniciadoEn(), e.getResultadoDisponibleEn()),
                d(e.getAnalisisIniciadoEn(), e.getVariablesPreparadasEn()),
                d(e.getVariablesPreparadasEn(), e.getModeloSolicitadoEn()),
                d(e.getModeloSolicitadoEn(), e.getModeloRespondioEn()),
                d(e.getModeloRespondioEn(), e.getResultadoDisponibleEn()),
                e.getPrediccionModelo() == null ? null : e.getPrediccionModelo().getId(),
                e.getOrigenResultado() == null ? null : e.getOrigenResultado().name(),
                e.getEstadoValidez().name());
    }
}
