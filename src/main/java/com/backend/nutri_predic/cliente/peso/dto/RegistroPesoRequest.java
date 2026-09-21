package com.backend.nutri_predic.cliente.peso.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegistroPesoRequest(
        @NotNull @DecimalMin("1.0") @DecimalMax("500.0") @Digits(integer = 3, fraction = 2, message = "El peso admite hasta dos decimales") BigDecimal pesoKg,
        boolean confirmarCambioAnomalo) {}
