package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.PeriodoFrecuencia;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SuplementoActualizacionRequest(
        @NotBlank @Size(max = 255) String nombreSuplemento,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) Double cantidad,
        @NotBlank @Size(max = 50) String unidad,
        @Size(max = 150) String frecuencia,
        @NotBlank @Size(max = 150) String tiempoUso,
        @NotNull Boolean activo,
        @NotNull LocalDate fechaInicio,
        LocalDate fechaFin,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal cantidadPorToma,
        @Size(max = 30) String unidadCodigo,
        @Positive Integer tomasPorPeriodo,
        PeriodoFrecuencia periodoFrecuencia,
        @Size(max = 1000) String componentesDeclarados,
        @DecimalMin(value = "0.0") BigDecimal energiaKcalPorToma,
        @DecimalMin(value = "0.0") BigDecimal proteinaGPorToma,
        @DecimalMin(value = "0.0") BigDecimal carbohidratosGPorToma,
        @DecimalMin(value = "0.0") BigDecimal grasasGPorToma,
        @DecimalMin(value = "0.0") BigDecimal creatinaGPorToma,
        @DecimalMin(value = "0.0") BigDecimal cafeinaMgPorToma,
        @DecimalMin(value = "0.0") BigDecimal sodioMgPorToma) {
    public SuplementoActualizacionRequest(
            double cantidad,
            String unidad,
            String frecuencia,
            String tiempoUso,
            boolean activo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal cantidadPorToma,
            String unidadCodigo,
            int tomasPorPeriodo,
            PeriodoFrecuencia periodoFrecuencia) {
        this(
                null,
                cantidad,
                unidad,
                frecuencia,
                tiempoUso,
                activo,
                fechaInicio,
                fechaFin,
                cantidadPorToma,
                unidadCodigo,
                tomasPorPeriodo,
                periodoFrecuencia,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
