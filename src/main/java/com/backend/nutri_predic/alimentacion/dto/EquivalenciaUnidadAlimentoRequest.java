package com.backend.nutri_predic.alimentacion.dto;

import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

public record EquivalenciaUnidadAlimentoRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidadOrigen,
        @NotBlank String unidadOrigenCodigo,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidadDestino,
        @NotBlank String unidadDestinoCodigo,
        String fuenteDatos,
        @NotNull LocalDate fechaDesde,
        LocalDate fechaHasta,
        Boolean activo) {}
