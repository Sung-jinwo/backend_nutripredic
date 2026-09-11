package com.backend.nutri_predic.indicador.service;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.consumo.entity.EstadoClasificacionConsumo;
import com.backend.nutri_predic.consumo.entity.EvaluacionConsumo;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcs;
import com.backend.nutri_predic.indicador.dto.PcsIndicatorResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PcsIndicatorService {
    public static final String CLASIFICACION_METODOLOGICA_NO_DISPONIBLE =
            "CLASIFICACION_METODOLOGICA_NO_DISPONIBLE";
    private final EvaluacionConsumoRepository evaluaciones;

    public PcsIndicatorService(EvaluacionConsumoRepository evaluaciones) {
        this.evaluaciones = evaluaciones;
    }

    @Transactional(readOnly = true)
    public PcsIndicatorResponse obtener() {
        List<EvaluacionConsumo> candidatas =
                evaluaciones
                        .findCandidatasPcsOficial(
                                EstadoValidezMedicion.VALIDA,
                                List.of(
                                        EstadoClasificacionConsumo.ALTO,
                                        EstadoClasificacionConsumo.NO_ALTO))
                        .stream()
                        .filter(this::clasificacionCoherente)
                        .toList();

        if (candidatas.isEmpty()) {
            return noDisponible(0, 0, CLASIFICACION_METODOLOGICA_NO_DISPONIBLE);
        }

        Map<Long, List<EvaluacionConsumo>> porCliente =
                candidatas.stream()
                        .collect(Collectors.groupingBy(item -> item.getCliente().getId()));
        List<EvaluacionConsumo> seleccionadas = porCliente.values().stream()
                .map(lista -> lista.stream().max(Comparator
                        .comparing(EvaluacionConsumo::getFechaCorte)
                        .thenComparing(EvaluacionConsumo::getFechaEvaluacion)
                        .thenComparing(EvaluacionConsumo::getId)).orElseThrow())
                .toList();

        long totalEvaluados = seleccionadas.size();
        long totalAlto =
                seleccionadas.stream()
                        .filter(item -> Boolean.TRUE.equals(item.getAltoConsumo()))
                        .count();
        double porcentaje = totalAlto * 100.0 / totalEvaluados;
        return new PcsIndicatorResponse(
                porcentaje, totalEvaluados, totalAlto, EstadoDisponibilidadPcs.DISPONIBLE, null);
    }

    private boolean clasificacionCoherente(EvaluacionConsumo evaluacion) {
        return evaluacion.getEstadoClasificacion() == EstadoClasificacionConsumo.ALTO
                        && Boolean.TRUE.equals(evaluacion.getAltoConsumo())
                || evaluacion.getEstadoClasificacion() == EstadoClasificacionConsumo.NO_ALTO
                        && Boolean.FALSE.equals(evaluacion.getAltoConsumo());
    }

    private PcsIndicatorResponse noDisponible(long totalEvaluados, long totalAlto, String motivo) {
        return new PcsIndicatorResponse(
                null, totalEvaluados, totalAlto, EstadoDisponibilidadPcs.NO_DISPONIBLE, motivo);
    }
}
