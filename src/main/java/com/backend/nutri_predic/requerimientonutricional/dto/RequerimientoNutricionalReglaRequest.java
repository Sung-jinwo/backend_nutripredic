package com.backend.nutri_predic.requerimientonutricional.dto;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.requerimientonutricional.entity.EstadoReglaRequerimientoNutricional;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record RequerimientoNutricionalReglaRequest(
        @NotNull EstadoReglaRequerimientoNutricional estado, @NotNull Boolean validada,
        @NotBlank @Size(max = 300) String fuenteReferencia, @NotBlank @Size(max = 100) String versionReferencia,
        LocalDate vigenteDesde, LocalDate vigenteHasta,
        @NotNull @Min(0) @Max(120) Integer edadMinima, @NotNull @Min(0) @Max(120) Integer edadMaxima,
        @DecimalMin("0.01") BigDecimal pesoMinimoKg, @DecimalMin("0.01") BigDecimal pesoMaximoKg,
        @DecimalMin("0.01") BigDecimal alturaMinimaCm, @DecimalMin("0.01") BigDecimal alturaMaximaCm,
        @NotNull Boolean realizaActividadFisica, @Min(0) @Max(7) Integer diasEntrenamientoMinimo,
        @Min(0) @Max(7) Integer diasEntrenamientoMaximo, @Size(max = 120) String tipoActividadFisica,
        @Min(1) @Max(1440) Integer duracionSesionMinima, @Min(1) @Max(1440) Integer duracionSesionMaxima,
        @NotNull ObjetivoEnergetico objetivoEnergetico,
        @NotNull @DecimalMin("0.01") BigDecimal kcalObjetivo, @NotNull @DecimalMin("0.01") BigDecimal proteinaObjetivoG,
        @NotNull @DecimalMin("0.01") BigDecimal carbohidratosObjetivoG, @NotNull @DecimalMin("0.01") BigDecimal grasasObjetivoG) {}
