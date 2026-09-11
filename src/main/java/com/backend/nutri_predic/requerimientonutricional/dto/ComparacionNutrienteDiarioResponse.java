package com.backend.nutri_predic.requerimientonutricional.dto;
import java.math.BigDecimal;
public record ComparacionNutrienteDiarioResponse(
        BigDecimal objetivo, BigDecimal consumido, BigDecimal diferencia, BigDecimal porcentajeCumplimiento) {}
