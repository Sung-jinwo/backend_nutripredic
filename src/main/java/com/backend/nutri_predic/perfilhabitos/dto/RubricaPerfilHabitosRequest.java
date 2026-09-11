package com.backend.nutri_predic.perfilhabitos.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record RubricaPerfilHabitosRequest(
        @NotBlank @Size(max = 80) String codigo,
        @NotNull @DecimalMin("0.01") BigDecimal version,
        @NotBlank @Size(max = 255) String nombre,
        @Size(max = 2000) String descripcion,
        Instant vigenteDesde,
        Instant vigenteHasta,
        @Size(max = 255) String validadoPor,
        Instant validadoEn,
        @Size(max = 2000) String observacionValidacion) {
    public RubricaPerfilHabitosRequest(
            String codigo,
            Integer version,
            String nombre,
            String descripcion,
            Instant vigenteDesde,
            String validadoPor,
            Instant validadoEn) {
        this(
                codigo,
                version == null ? null : BigDecimal.valueOf(version.longValue()),
                nombre,
                descripcion,
                vigenteDesde,
                null,
                validadoPor,
                validadoEn,
                null);
    }
}
