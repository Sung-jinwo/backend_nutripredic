package com.backend.nutri_predic.perfilhabitos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PuntuacionManualCriterioRequest(
        @NotBlank @Size(max = 80) String codigoCriterio,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntosObtenidos,
        @Size(max = 2000) String observacion) {}
