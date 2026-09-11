package com.backend.nutri_predic.alimentacion.dto;

import jakarta.validation.constraints.*;

public record AlimentoCatalogoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 100) String categoria,
        @Size(max = 30) String unidadBaseCodigo,
        Boolean activo) {}
