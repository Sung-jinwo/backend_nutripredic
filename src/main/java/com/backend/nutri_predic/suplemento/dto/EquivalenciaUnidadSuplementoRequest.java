package com.backend.nutri_predic.suplemento.dto;

import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

public record EquivalenciaUnidadSuplementoRequest(
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidadOrigen,
        @NotBlank String unidadOrigenCodigo,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidadDestino,
        @NotBlank String unidadDestinoCodigo,
        @NotNull LocalDate fechaDesde,
        LocalDate fechaHasta,
        Boolean activo) {}
