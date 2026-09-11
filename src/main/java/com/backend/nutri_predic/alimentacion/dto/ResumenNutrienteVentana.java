package com.backend.nutri_predic.alimentacion.dto;

import java.math.BigDecimal;

public record ResumenNutrienteVentana(
        BigDecimal totalConocido,
        Boolean completo,
        BigDecimal coberturaPorcentaje,
        int registrosConValor,
        int registrosTotales,
        BigDecimal promedioSobreVentanaConocido,
        BigDecimal promedioSobreDiasConNutricionConocido) {}
