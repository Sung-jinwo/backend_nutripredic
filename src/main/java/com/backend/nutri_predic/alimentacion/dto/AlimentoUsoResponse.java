package com.backend.nutri_predic.alimentacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AlimentoUsoResponse(
        Long alimentoId,
        String nombre,
        String categoria,
        BigDecimal ultimaCantidad,
        String ultimaUnidad,
        String ultimoMomento,
        LocalDate ultimaFecha,
        long vecesUtilizado,
        BigDecimal ultimaProteinaG,
        BigDecimal ultimosCarbohidratosG,
        BigDecimal ultimasGrasasG) {}
