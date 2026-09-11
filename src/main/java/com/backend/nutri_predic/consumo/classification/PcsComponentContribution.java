package com.backend.nutri_predic.consumo.classification;

import java.math.BigDecimal;

public record PcsComponentContribution(
        String componente,
        BigDecimal aporteAlimentos,
        boolean alimentosCalculables,
        BigDecimal aporteSuplementos,
        boolean suplementosCalculables,
        String unidad) {}
