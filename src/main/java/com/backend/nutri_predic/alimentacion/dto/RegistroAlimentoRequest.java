package com.backend.nutri_predic.alimentacion.dto;

import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegistroAlimentoRequest(
        @Positive Long alimentoId,
        @NotBlank @Size(max = 200) String nombreAlimento,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidad,
        @NotBlank @Size(max = 30) String unidadCodigo,
        @NotNull MomentoComida momentoComida,
        @NotNull @DecimalMin("0.0") BigDecimal proteinaG,
        @NotNull @DecimalMin("0.0") BigDecimal carbohidratosG,
        @NotNull @DecimalMin("0.0") BigDecimal grasasG) {
    public RegistroAlimentoRequest(
            Long alimentoId,
            BigDecimal cantidad,
            String unidadCodigo,
            MomentoComida momentoComida) {
        this(alimentoId, null, cantidad, unidadCodigo, momentoComida, null, null, null);
    }
}
