package com.backend.nutri_predic.cliente.peso.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegistroPesoRequest(
        @NotNull @DecimalMin("1.0") @DecimalMax("500.0") BigDecimal pesoKg,
        boolean confirmarCambioAnomalo) {}
