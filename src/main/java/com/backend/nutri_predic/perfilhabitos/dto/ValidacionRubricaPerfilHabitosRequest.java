package com.backend.nutri_predic.perfilhabitos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ValidacionRubricaPerfilHabitosRequest(
        @NotBlank @Size(max = 255) String validadoPor,
        @NotNull Instant validadoEn,
        @Size(max = 2000) String observacionValidacion,
        @NotNull Instant vigenteDesde,
        Instant vigenteHasta) {}
