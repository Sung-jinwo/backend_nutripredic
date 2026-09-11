package com.backend.nutri_predic.perfilhabitos.dto;

import com.backend.nutri_predic.perfilhabitos.entity.EstadoRubricaPerfilHabitos;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRubricaRequest(@NotNull EstadoRubricaPerfilHabitos estado) {}
