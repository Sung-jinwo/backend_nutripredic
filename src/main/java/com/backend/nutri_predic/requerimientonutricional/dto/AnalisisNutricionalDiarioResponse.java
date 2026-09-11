package com.backend.nutri_predic.requerimientonutricional.dto;
import java.time.LocalDate;
public record AnalisisNutricionalDiarioResponse(
        Long clienteId, LocalDate fecha, String estado, RequerimientoNutricionalResponse requerimiento,
        ComparacionNutrienteDiarioResponse kcal, ComparacionNutrienteDiarioResponse proteinaG,
        ComparacionNutrienteDiarioResponse carbohidratosG, ComparacionNutrienteDiarioResponse grasasG) {}
