package com.backend.nutri_predic.perfilhabitos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CrearEvaluacionPerfilHabitosV6Request(
        @Valid List<PuntuacionManualCriterioRequest> criteriosManuales,
        @Size(max = 2000) String observacion) {}
