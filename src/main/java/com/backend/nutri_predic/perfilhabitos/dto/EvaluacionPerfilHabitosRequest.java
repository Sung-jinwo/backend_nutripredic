package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EvaluacionPerfilHabitosRequest(
        @NotNull @Positive Long clienteId,
        @NotNull @Positive Long rubricaId,
        @NotNull @PastOrPresent LocalDate fechaCorte,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntajeTotal,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntajeMaximoCalculable,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal coberturaCalculable,
        @NotNull EstadoValidezMedicion estadoValidez,
        @Size(max = 1000) String motivoNoValida,
        @Size(max = 2000) String observacion,
        List<ResultadoDimensionPerfilHabitosRequest> dimensiones,
        List<ResultadoCriterioPerfilHabitosRequest> criterios) {}
