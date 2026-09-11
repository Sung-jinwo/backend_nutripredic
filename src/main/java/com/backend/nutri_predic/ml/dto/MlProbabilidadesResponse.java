package com.backend.nutri_predic.ml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Probabilidades normalizadas de las tres clases V5")
public record MlProbabilidadesResponse(
        @JsonProperty("ADECUADO") BigDecimal adecuado,
        @JsonProperty("MEJORABLE") BigDecimal mejorable,
        @JsonProperty("CRITICO") BigDecimal critico) {}
