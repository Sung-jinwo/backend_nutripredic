package com.backend.nutri_predic.conocimiento.practica.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "Entrega completa de respuestas adaptativas complementarias")
public record ResponderConocimientoIaRequest(@NotEmpty List<@Valid Respuesta> respuestas) {

    @JsonAnySetter
    public void rechazarCampoNoPermitido(String nombre, Object valor) {
        throw new IllegalArgumentException("Campo no permitido: " + nombre);
    }

    public record Respuesta(
            @NotNull @Positive Long preguntaId,
            @NotBlank @Pattern(regexp = "(?i)[A-D]", message = "debe ser A, B, C o D")
                    String opcionSeleccionada) {

        @JsonAnySetter
        public void rechazarCampoNoPermitido(String nombre, Object valor) {
            throw new IllegalArgumentException("Campo no permitido: " + nombre);
        }
    }
}
