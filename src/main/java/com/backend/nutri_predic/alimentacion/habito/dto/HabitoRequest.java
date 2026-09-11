package com.backend.nutri_predic.alimentacion.habito.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record HabitoRequest(
        @NotNull Long clienteId,
        @NotNull @PastOrPresent LocalDate fecha,
        @Min(1) @Max(20) Integer cantidadComidas,
        @DecimalMin("0.0") Double consumoAgua,
        @DecimalMin("0.0") Double proteinas,
        @Size(max = 100) String tipoAlimentacion,
        @Size(max = 100) String nivelOrganizacion,
        Boolean desayuno,
        Boolean snacks,
        @Size(max = 2000) String alimentos,
        @Min(0) @Max(20) Integer comidasCocinadas,
        @Size(max = 1000) String restricciones,
        Boolean consumeSuplementos) {}
