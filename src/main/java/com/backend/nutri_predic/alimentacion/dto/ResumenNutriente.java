package com.backend.nutri_predic.alimentacion.dto;

import java.math.BigDecimal;

/** Valor conocido y su calidad; un valor parcial nunca representa un total completo. */
public record ResumenNutriente(
        BigDecimal valorConocido,
        Boolean completo,
        BigDecimal coberturaPorcentaje,
        int registrosConValor,
        int registrosTotales) {}
