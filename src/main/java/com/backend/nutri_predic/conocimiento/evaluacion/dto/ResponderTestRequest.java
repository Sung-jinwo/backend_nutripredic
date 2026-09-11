package com.backend.nutri_predic.conocimiento.evaluacion.dto;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ResponderTestRequest(
        @NotNull @Positive Long clienteId,
        Long instrumentoId,
        MomentoEvaluacion momento,
        @NotEmpty List<@Valid Respuesta> respuestas) {
    public ResponderTestRequest(Long clienteId, List<Respuesta> respuestas) {
        this(clienteId, null, MomentoEvaluacion.NO_DETERMINADO, respuestas);
    }

    public record Respuesta(
            @NotNull @Positive Long preguntaId,
            @NotBlank @Pattern(regexp = "(?i)[A-D]", message = "debe ser A, B, C o D")
                    String opcion) {}
}
