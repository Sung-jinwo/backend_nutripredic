package com.backend.nutri_predic.consumo.classification;

import java.math.BigDecimal;

public record PcsObservedValue(
        Long suplementoId,
        String componenteTipo,
        String elementoAplicable,
        String metrica,
        BigDecimal datoObservado,
        String unidad,
        boolean normalizado) {}
