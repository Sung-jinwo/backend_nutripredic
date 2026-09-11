package com.backend.nutri_predic.perfilhabitos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ResultadoDimensionPerfilHabitosRequest(
        @NotNull @Positive Long dimensionId,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntosObtenidos,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntosMaximosCalculables,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal coberturaCalculable,
        @Size(max = 2000) String criteriosNoCalculables,
        @Size(max = 2000) String observacion) {}
