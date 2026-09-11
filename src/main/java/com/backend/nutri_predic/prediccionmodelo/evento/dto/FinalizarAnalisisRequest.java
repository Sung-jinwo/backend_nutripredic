package com.backend.nutri_predic.prediccionmodelo.evento.dto;

import jakarta.validation.constraints.Size;

public record FinalizarAnalisisRequest(@Size(max = 2000) String observacionesTecnicas) {}
