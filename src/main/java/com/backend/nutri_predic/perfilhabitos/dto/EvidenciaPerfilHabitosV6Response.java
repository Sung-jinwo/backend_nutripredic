package com.backend.nutri_predic.perfilhabitos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EvidenciaPerfilHabitosV6Response(
        Long clienteId,
        LocalDate fechaCorte,
        Long rubricaId,
        String rubricaCodigo,
        BigDecimal rubricaVersion,
        String estadoRubrica,
        Boolean rubricaValidada,
        BigDecimal puntajeAutomaticoCalculable,
        BigDecimal puntajeMaximoCalculable,
        BigDecimal coberturaCalculable,
        String estadoValidezSugerido,
        List<EvidenciaDimension> dimensiones,
        List<EvidenciaCriterio> criterios) {
    public record EvidenciaDimension(
            Long dimensionId,
            String codigo,
            String nombre,
            BigDecimal pesoConfigurado,
            BigDecimal puntosObtenidos,
            BigDecimal puntosMaximosCalculables,
            BigDecimal coberturaCalculable) {}

    public record EvidenciaCriterio(
            Long criterioId,
            Long dimensionId,
            String codigo,
            String nombre,
            String tipoEvaluacion,
            String estado,
            BigDecimal valorObservado,
            BigDecimal puntosObtenidos,
            BigDecimal puntosMaximos,
            Integer diasCumple,
            String referenciaAplicada,
            String motivoNoCalculable,
            String fuente,
            String tipoFuente) {}
}
