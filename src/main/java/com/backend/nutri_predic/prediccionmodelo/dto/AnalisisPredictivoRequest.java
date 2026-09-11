package com.backend.nutri_predic.prediccionmodelo.dto;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Solicitud del análisis predictivo oficial V5")
public record AnalisisPredictivoRequest(
        @Schema(example = "42") @NotNull @Positive Long clienteId,
        @Schema(example = "2026-08-30") @NotNull @PastOrPresent LocalDate fechaCorte,
        @Schema(
                        description =
                                "Participación experimental opcional; omitir cuando no aplique",
                        nullable = true)
                @Positive
                Long participacionEstudioId,
        @Schema(allowableValues = {"BASAL", "FINAL", "NO_DETERMINADO"}) @NotNull
                MomentoEvaluacion momento) {}
