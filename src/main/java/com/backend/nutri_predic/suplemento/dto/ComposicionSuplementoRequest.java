package com.backend.nutri_predic.suplemento.dto;

import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

public record ComposicionSuplementoRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidadPorcionReferencia,
        @NotBlank String unidadPorcionCodigo,
        @DecimalMin(value = "0", inclusive = false) BigDecimal energiaKcalPorcion,
        @NotNull LocalDate fechaDesde,
        LocalDate fechaHasta,
        Boolean activo,
        String fuenteDatos) {
    public ComposicionSuplementoRequest(BigDecimal cantidadPorcionReferencia, String unidadPorcionCodigo, LocalDate fechaDesde, LocalDate fechaHasta, Boolean activo, String fuenteDatos) {
        this(cantidadPorcionReferencia, unidadPorcionCodigo, null, fechaDesde, fechaHasta, activo, fuenteDatos);
    }
}
