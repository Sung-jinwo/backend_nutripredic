package com.backend.nutri_predic.alimentacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ComposicionNutricionalAlimentoRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidadReferencia,
        @NotBlank @Size(max = 30) String unidadReferenciaCodigo,
        @DecimalMin(value = "0.0") BigDecimal kcal,
        @DecimalMin(value = "0.0") BigDecimal proteinaG,
        @DecimalMin(value = "0.0") BigDecimal carbohidratosG,
        @DecimalMin(value = "0.0") BigDecimal grasasG,
        @DecimalMin(value = "0.0") BigDecimal fibraG,
        @DecimalMin(value = "0.0") BigDecimal azucarG,
        @DecimalMin(value = "0.0") BigDecimal sodioMg,
        @Size(max = 1000) String fuenteDatos,
        @NotNull LocalDate fechaDesde,
        LocalDate fechaHasta,
        Boolean activo) {}
