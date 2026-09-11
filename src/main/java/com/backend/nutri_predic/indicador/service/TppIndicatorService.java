package com.backend.nutri_predic.indicador.service;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadTpp;
import com.backend.nutri_predic.indicador.dto.MotivoExclusionTpp;
import com.backend.nutri_predic.indicador.dto.TppIndicatorResponse;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.TipoProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TppIndicatorService {
    public static final String SIN_ANALISIS_VALIDOS = "SIN_ANALISIS_VALIDOS";

    private final EventoAnalisisRepository eventos;

    public TppIndicatorService(EventoAnalisisRepository eventos) {
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public TppIndicatorResponse obtener() {
        long sumaTppMs = 0;
        long totalValidos = 0;
        EnumMap<MotivoExclusionTpp, Long> exclusiones = new EnumMap<>(MotivoExclusionTpp.class);

        for (EventoAnalisis evento : eventos.findAll()) {
            MotivoExclusionTpp motivo = motivoExclusion(evento);
            if (motivo != null) {
                exclusiones.merge(motivo, 1L, Long::sum);
                continue;
            }

            sumaTppMs +=
                    Duration.between(
                                    evento.getAnalisisIniciadoEn(),
                                    evento.getResultadoDisponibleEn())
                            .toMillis();
            totalValidos++;
        }

        long totalExcluidos = exclusiones.values().stream().mapToLong(Long::longValue).sum();
        Map<MotivoExclusionTpp, Long> trazabilidad = Map.copyOf(exclusiones);

        if (totalValidos == 0) {
            return new TppIndicatorResponse(
                    null,
                    0,
                    totalExcluidos,
                    EstadoDisponibilidadTpp.NO_DISPONIBLE,
                    SIN_ANALISIS_VALIDOS,
                    trazabilidad);
        }

        return new TppIndicatorResponse(
                sumaTppMs / (double) totalValidos,
                totalValidos,
                totalExcluidos,
                EstadoDisponibilidadTpp.DISPONIBLE,
                null,
                trazabilidad);
    }

    private MotivoExclusionTpp motivoExclusion(EventoAnalisis evento) {
        if (evento.getProcedimiento() == null
                || evento.getProcedimiento().getTipo() != TipoProcedimientoAnalisis.SOFTWARE_IA) {
            return MotivoExclusionTpp.PROCEDIMIENTO_NO_APLICABLE;
        }
        if (evento.getAnalisisIniciadoEn() == null) {
            return MotivoExclusionTpp.INICIO_AUSENTE;
        }
        if (evento.getResultadoDisponibleEn() == null) {
            return MotivoExclusionTpp.RESULTADO_AUSENTE;
        }
        if (evento.getResultadoDisponibleEn().isBefore(evento.getAnalisisIniciadoEn())) {
            return MotivoExclusionTpp.ORDEN_TEMPORAL_INVALIDO;
        }
        if (evento.getEstadoValidez() != EstadoValidezMedicion.VALIDA) {
            return MotivoExclusionTpp.EVENTO_INVALIDO;
        }
        return null;
    }
}
