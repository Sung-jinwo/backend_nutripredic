package com.backend.nutri_predic.consumo.dto;

import com.backend.nutri_predic.consumo.classification.AmbitoAportePcs;
import com.backend.nutri_predic.consumo.classification.OperadorCriterioPcs;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CriterioConsumoSuplementosRequest(
        @NotBlank @Size(max = 30) String alcance,
        Long suplementoId,
        @Size(max = 50) String componenteTipo,
        @Size(max = 255) String elementoAplicable,
        @Size(max = 100) String metrica,
        BigDecimal cantidadReferencia,
        BigDecimal cantidadReferenciaHasta,
        AmbitoAportePcs ambitoAporte,
        @Size(max = 30) String unidadReferencia,
        @Size(max = 30) String unidadNormalizada,
        @Size(max = 80) String frecuencia,
        @Positive Integer numeroTomas,
        @Positive Integer ventanaDias,
        boolean requiereComposicion,
        OperadorCriterioPcs operador,
        @Size(max = 4000) String parametros,
        @Size(max = 1000) String fuenteReferencia,
        @Size(max = 100) String versionReferencia,
        @Size(max = 2000) String observacionMetodologica) {}
