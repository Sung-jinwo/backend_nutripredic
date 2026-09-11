package com.backend.nutri_predic.consumo.dto;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import jakarta.validation.constraints.*;
import java.time.*;

public record EvaluacionConsumoRequest(
        @NotNull @Positive Long clienteId,
        @NotNull LocalDate fechaCorte,
        @NotNull @Min(1) @Max(31) Integer ventanaDias,
        MomentoEvaluacion momento) {}
