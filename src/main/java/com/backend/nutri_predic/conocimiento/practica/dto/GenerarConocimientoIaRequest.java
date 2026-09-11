package com.backend.nutri_predic.conocimiento.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "Configuración permitida para preguntas adaptativas complementarias")
public record GenerarConocimientoIaRequest(
        @NotEmpty List<@NotBlank String> temasPermitidos,
        @NotBlank String dificultadPermitida,
        @Min(1) @Max(10) int cantidadPreguntas) {}
