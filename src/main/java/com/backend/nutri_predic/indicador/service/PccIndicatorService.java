package com.backend.nutri_predic.indicador.service;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcc;
import com.backend.nutri_predic.indicador.dto.PccIndicatorResponse;
import com.backend.nutri_predic.conocimiento.evaluacion.entity.ResultadoTest;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.ResultadoTestRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PccIndicatorService {
    public static final String SIN_RESULTADOS_VALIDOS = "SIN_RESULTADOS_VALIDOS";

    private final ResultadoTestRepository resultados;

    public PccIndicatorService(ResultadoTestRepository resultados) {
        this.resultados = resultados;
    }

    @Transactional(readOnly = true)
    public PccIndicatorResponse obtener() {
        Map<Long, ResultadoTest> ultimoValidoPorCliente = new LinkedHashMap<>();
        Comparator<ResultadoTest> porFechaEId =
                Comparator.comparing(
                                ResultadoTest::getFecha,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(
                                ResultadoTest::getId,
                                Comparator.nullsFirst(Comparator.naturalOrder()));

        for (ResultadoTest resultado :
                resultados.findByEstadoValidezAndNivelIsNotNull(EstadoValidezMedicion.VALIDA)) {
            ultimoValidoPorCliente.merge(
                    resultado.getCliente().getId(),
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
                        .filter(resultado -> resultado.getNivel() == NivelConocimiento.BAJO)
                        .count();
        return new PccIndicatorResponse(
                totalBajo * 100.0 / totalEvaluados,
                totalEvaluados,
                totalBajo,
                EstadoDisponibilidadPcc.DISPONIBLE,
                null);
    }
}
