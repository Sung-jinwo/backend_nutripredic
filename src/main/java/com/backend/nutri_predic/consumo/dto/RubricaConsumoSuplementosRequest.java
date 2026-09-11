package com.backend.nutri_predic.consumo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record RubricaConsumoSuplementosRequest(
        @NotBlank @Size(max = 80) String codigo,
        @NotNull @Positive Integer version,
        @NotNull @Positive Integer ventanaDias,
        Instant vigenteDesde,
        Instant vigenteHasta,
        @NotNull Boolean validada,
        @Size(max = 2000) String observacion,
        @Size(max = 1000) String fuenteReferencia,
        @Size(max = 255) String validadoPor,
        Instant validadoEn,
        @Size(max = 80) String versionEvaluador,
        @Size(max = 4000) String metadataValidacion,
        @Size(max = 4000) String reglaGlobal) {}
