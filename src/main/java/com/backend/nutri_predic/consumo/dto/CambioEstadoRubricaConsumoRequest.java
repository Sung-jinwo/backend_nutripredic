package com.backend.nutri_predic.consumo.dto;

import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRubricaConsumoRequest(@NotNull EstadoCriterioConsumo estado) {}
