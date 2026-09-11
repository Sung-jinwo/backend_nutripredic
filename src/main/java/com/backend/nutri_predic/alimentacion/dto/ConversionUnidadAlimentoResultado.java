package com.backend.nutri_predic.alimentacion.dto;

import java.math.*;

public record ConversionUnidadAlimentoResultado(
        boolean calculable,
        BigDecimal cantidadConvertida,
        String unidadDestino,
        Long equivalenciaId) {
    public static ConversionUnidadAlimentoResultado noCalculable() {
        return new ConversionUnidadAlimentoResultado(false, null, null, null);
    }
}
