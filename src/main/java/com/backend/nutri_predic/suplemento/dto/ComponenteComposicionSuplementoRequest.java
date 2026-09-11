package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.TipoComponenteSuplemento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ComponenteComposicionSuplementoRequest(
        @NotNull TipoComponenteSuplemento tipo,
        @Size(max = 200) String nombreOtro,
        @NotNull @DecimalMin("0.0") BigDecimal cantidad,
        @NotBlank String unidadCodigo) {}
