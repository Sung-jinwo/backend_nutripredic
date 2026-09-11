package com.backend.nutri_predic.suplemento.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegistroConsumoSuplementoRequest(
        @NotNull @Positive Long suplementoClienteId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidadConsumida,
        @NotBlank @Size(max = 30) String unidadCodigo,
        @Positive Integer numeroTomas,
        @Size(max = 500) String observacion) {}
