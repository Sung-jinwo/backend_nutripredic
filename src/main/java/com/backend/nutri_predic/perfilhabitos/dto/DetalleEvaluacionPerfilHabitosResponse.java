package com.backend.nutri_predic.perfilhabitos.dto;

import java.util.List;

public record DetalleEvaluacionPerfilHabitosResponse(
        EvaluacionPerfilHabitosResponse evaluacion,
        List<ResultadoDimensionPerfilHabitosResponse> dimensiones,
        List<ResultadoCriterioPerfilHabitosResponse> criterios) {}
