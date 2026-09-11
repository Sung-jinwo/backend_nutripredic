package com.backend.nutri_predic.alimentacion.nutricion.dto;

import java.math.BigDecimal;

/** valor null significa que no se pudo obtener una composición completa. */
public record NutrienteDiarioResponse(BigDecimal valor, boolean calculable) {}
