package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.CodigoDimensionPerfilHabitos;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record DimensionRubricaPerfilHabitosRequest(
        @NotNull CodigoDimensionPerfilHabitos codigo,
        @NotBlank @Size(max = 255) String nombre,
        @Size(max = 2000) String descripcion,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntajeMinimo,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntajeMaximo,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal pesoPorcentual,
        @NotNull @Positive Integer orden) {}
