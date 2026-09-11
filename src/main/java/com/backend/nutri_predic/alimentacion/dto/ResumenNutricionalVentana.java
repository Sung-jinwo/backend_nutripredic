package com.backend.nutri_predic.alimentacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenNutricionalVentana(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        int diasVentana,
        int diasConRegistroHabito,
        int diasConRegistroAlimento,
        int diasConNutricionCalculable,
        int totalRegistrosAlimento,
        int registrosCalculables,
        int registrosNoCalculables,
        BigDecimal porcentajeRegistrosCalculables,
        ResumenNutrienteVentana kcal,
        ResumenNutrienteVentana proteinaG,
        ResumenNutrienteVentana carbohidratosG,
        ResumenNutrienteVentana grasasG,
        ResumenNutrienteVentana fibraG,
        ResumenNutrienteVentana azucarG,
        ResumenNutrienteVentana sodioMg,
        BigDecimal pesoAplicableKg,
        String fuentePeso,
        BigDecimal proteinaGKgDiaSobreVentanaConocido,
        BigDecimal proteinaGKgDiaSobreDiasConNutricionConocido) {}
