package com.backend.nutri_predic.suplemento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Compatibility request for the client/date consumption endpoint. */
public record RegistroConsumoSuplementoClienteRequest(
        @NotNull @Positive Long suplementoClienteId,
        @NotNull LocalDate fecha,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidad,
        @NotBlank @Size(max = 30) String unidad,
        @NotBlank @Size(max = 20) String estado) {}
