package com.backend.nutri_predic.alimentacion.dto;

import com.backend.nutri_predic.alimentacion.entity.ComposicionNutricionalAlimento;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ComposicionNutricionalAlimentoResponse(
        Long id,
        Long alimentoId,
        Integer version,
        BigDecimal cantidadReferencia,
        String unidadReferencia,
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal fibraG,
        BigDecimal azucarG,
        BigDecimal sodioMg,
        String fuenteDatos,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        boolean activo) {
    public static ComposicionNutricionalAlimentoResponse from(ComposicionNutricionalAlimento c) {
        return new ComposicionNutricionalAlimentoResponse(
                c.getId(),
                c.getAlimento().getId(),
                c.getVersion(),
                c.getCantidadReferencia(),
                c.getUnidadReferencia().getCodigo(),
                c.getKcal(),
                c.getProteinaG(),
                c.getCarbohidratosG(),
                c.getGrasasG(),
                c.getFibraG(),
                c.getAzucarG(),
                c.getSodioMg(),
                c.getFuenteDatos(),
                c.getFechaDesde(),
                c.getFechaHasta(),
                c.isActivo());
    }
}
