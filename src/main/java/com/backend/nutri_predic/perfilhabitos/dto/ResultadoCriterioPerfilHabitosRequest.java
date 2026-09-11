package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.EstadoResultadoCriterioPerfil;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ResultadoCriterioPerfilHabitosRequest(
        @NotNull @Positive Long criterioId,
        @NotNull EstadoResultadoCriterioPerfil estado,
        @DecimalMin("0.00") BigDecimal valorObservado,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntosObtenidos,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal puntosMaximos,
        @Size(max = 1000) String referenciaAplicada,
        @Size(max = 1000) String motivoNoCalculable,
        @Size(max = 2000) String observacion) {}
