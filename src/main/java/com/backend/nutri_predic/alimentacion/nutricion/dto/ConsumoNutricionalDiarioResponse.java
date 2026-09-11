package com.backend.nutri_predic.alimentacion.nutricion.dto;

import java.time.LocalDate;

public record ConsumoNutricionalDiarioResponse(
        Long clienteId,
        LocalDate fecha,
        NutrienteDiarioResponse kcal,
        NutrienteDiarioResponse proteinaG,
        NutrienteDiarioResponse carbohidratosG,
        NutrienteDiarioResponse grasasG,
        int registrosAlimento,
        int registrosSuplemento) {}
