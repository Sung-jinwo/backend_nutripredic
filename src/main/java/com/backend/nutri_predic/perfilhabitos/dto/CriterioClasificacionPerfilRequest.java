package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CriterioClasificacionPerfilRequest(
        @NotNull ClasificacionPerfilHabitos clasificacion,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal limiteInferior,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal limiteSuperior,
        boolean incluyeInferior,
        boolean incluyeSuperior,
        @NotNull @Positive Integer orden) {}
