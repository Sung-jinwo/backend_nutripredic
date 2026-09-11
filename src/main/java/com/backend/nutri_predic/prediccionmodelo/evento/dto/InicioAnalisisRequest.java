package com.backend.nutri_predic.prediccionmodelo.evento.dto;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record InicioAnalisisRequest(
        @NotNull @Positive Long clienteId,
        @NotNull @Positive Long procedimientoId,
        @NotNull @PastOrPresent LocalDate fechaCorte,
        @Positive Long participacionEstudioId,
        @NotNull MomentoEvaluacion momento) {}
