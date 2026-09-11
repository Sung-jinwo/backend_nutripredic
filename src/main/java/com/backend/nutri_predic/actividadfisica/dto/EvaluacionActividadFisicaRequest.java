package com.backend.nutri_predic.actividadfisica.dto;
import jakarta.validation.constraints.*; import java.math.*; import java.time.*;
public record EvaluacionActividadFisicaRequest(@NotNull LocalDate fechaEvaluacion,@NotBlank String instrumentoCodigo,@NotBlank String instrumentoVersion,@NotBlank String nivelResultado,BigDecimal valorResultado,@NotBlank String fuenteReferencia,@NotNull Boolean validada,LocalDate vigenciaDesde,LocalDate vigenciaHasta){}
