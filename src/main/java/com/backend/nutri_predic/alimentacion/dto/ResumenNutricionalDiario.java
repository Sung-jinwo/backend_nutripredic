package com.backend.nutri_predic.alimentacion.dto;

import java.time.LocalDate;

public record ResumenNutricionalDiario(
        LocalDate fecha,
        ResumenNutriente kcal,
        ResumenNutriente proteinaG,
        ResumenNutriente carbohidratosG,
        ResumenNutriente grasasG,
        ResumenNutriente fibraG,
        ResumenNutriente azucarG,
        ResumenNutriente sodioMg,
        int registrosAlimentoTotal,
        int registrosAlimentoCalculables,
        int registrosAlimentoNoCalculables,
        java.math.BigDecimal porcentajeCoberturaNutricional) {}
