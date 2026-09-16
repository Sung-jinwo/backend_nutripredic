package com.backend.nutri_predic.indicador.service;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcc;
import com.backend.nutri_predic.indicador.dto.PccIndicatorResponse;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PccIndicatorService {
    public static final String SIN_RESULTADOS_VALIDOS = "SIN_RESULTADOS_VALIDOS";

    private final SesionConocimientoIaRepository sesiones;

    public PccIndicatorService(SesionConocimientoIaRepository sesiones) {
        this.sesiones = sesiones;
    }

    @Transactional(readOnly = true)
    public PccIndicatorResponse obtener() {
        Map<Long, SesionConocimientoIa> ultimoValidoPorCliente = new LinkedHashMap<>();
        Comparator<SesionConocimientoIa> porFechaEId =
                Comparator.comparing(
                                SesionConocimientoIa::getFechaEvaluacion,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(
                                SesionConocimientoIa::getRespondidaEn,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(
                                SesionConocimientoIa::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder()));

        for (SesionConocimientoIa resultado : sesiones.findByEstadoAndEstadoValidez(
                EstadoSesionConocimientoIa.RESPONDIDA, EstadoValidezMedicion.VALIDA)) {
            if (resultado.getClienteId() == null || resultado.getNivelResultado() == null) continue;
            ultimoValidoPorCliente.merge(
                    resultado.getClienteId(),
                    resultado,
                    (actual, candidato) ->
                            porFechaEId.compare(actual, candidato) >= 0 ? actual : candidato);
        }

        if (ultimoValidoPorCliente.isEmpty()) {
            return new PccIndicatorResponse(
                    null, 0, 0, EstadoDisponibilidadPcc.NO_DISPONIBLE, SIN_RESULTADOS_VALIDOS);
        }

        long totalEvaluados = ultimoValidoPorCliente.size();
        long totalBajo =
                ultimoValidoPorCliente.values().stream()
                        .filter(resultado -> "BAJO".equals(resultado.getNivelResultado()))
                        .count();
        return new PccIndicatorResponse(
                totalBajo * 100.0 / totalEvaluados,
                totalEvaluados,
                totalBajo,
                EstadoDisponibilidadPcc.DISPONIBLE,
                null);
    }
}
