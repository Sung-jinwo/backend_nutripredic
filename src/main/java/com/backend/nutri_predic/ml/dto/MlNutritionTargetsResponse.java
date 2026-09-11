package com.backend.nutri_predic.ml.dto;

import java.math.BigDecimal;

public record MlNutritionTargetsResponse(
        String estado,
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal aguaMl,
        String formulaNutricionalVersion,
        String fuenteFormulaNutricional,
        String motivo) {}
