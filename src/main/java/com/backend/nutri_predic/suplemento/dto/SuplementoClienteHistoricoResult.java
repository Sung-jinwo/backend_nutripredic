package com.backend.nutri_predic.suplemento.dto;

import java.math.*;
import java.time.*;

public record SuplementoClienteHistoricoResult(
        Long suplementoClienteId,
        Long suplementoId,
        BigDecimal cantidadPorToma,
        String unidadMedida,
        Integer tomasPorPeriodo,
        String periodoFrecuencia,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Boolean activo,
        String fuente,
        Instant fechaVersionAplicada) {}
