package com.backend.nutri_predic.conocimiento.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;

public record PreguntaRequest(
        @JsonAlias("enunciado") @NotBlank @Size(max = 2000) String texto,
        @JsonAlias("categoria") @NotBlank @Size(max = 255) String tema,
        @Size(max = 255) String subtema,
        @NotBlank @Size(max = 100) String dificultad,
        @NotBlank String opcionA,
        @NotBlank String opcionB,
        @NotBlank String opcionC,
        @NotBlank String opcionD,
        @NotBlank @Pattern(regexp = "(?i)[ABCD]") String respuestaCorrecta,
        @Size(max = 2000) String explicacion,
        @Size(max = 1000) String fuenteReferencia) {}
