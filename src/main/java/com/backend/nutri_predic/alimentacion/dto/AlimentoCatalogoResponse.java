package com.backend.nutri_predic.alimentacion.dto;

import com.backend.nutri_predic.alimentacion.entity.AlimentoCatalogo;
import com.backend.nutri_predic.alimentacion.entity.ComposicionNutricionalAlimento;
import java.math.BigDecimal;

public record AlimentoCatalogoResponse(
        Long id,
        String nombre,
        String categoria,
        String unidadBase,
        boolean activo,
        // Composición activa por referencia (expuesta a CLIENTE para preview sin ser ADMIN)
        BigDecimal cantidadReferencia,
        String unidadReferencia,
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal fibraG,
        BigDecimal azucarG,
        BigDecimal sodioMg,
        String porcionReferencia) {
    public static AlimentoCatalogoResponse from(AlimentoCatalogo a) {
        return new AlimentoCatalogoResponse(
                a.getId(),
                a.getNombre(),
                a.getCategoria(),
                a.getUnidadBase() == null ? null : a.getUnidadBase().getCodigo(),
                a.isActivo(),
                null, null, null, null, null, null, null, null, null, null);
    }

    public static AlimentoCatalogoResponse fromConComposicion(AlimentoCatalogo a, ComposicionNutricionalAlimento c) {
        if (c == null) return from(a);
        String porcion = c.getCantidadReferencia() + " " + c.getUnidadReferencia().getCodigo();
        return new AlimentoCatalogoResponse(
                a.getId(),
                a.getNombre(),
                a.getCategoria(),
                a.getUnidadBase() == null ? null : a.getUnidadBase().getCodigo(),
                a.isActivo(),
                c.getCantidadReferencia(),
                c.getUnidadReferencia().getCodigo(),
                c.getKcal(),
                c.getProteinaG(),
                c.getCarbohidratosG(),
                c.getGrasasG(),
                c.getFibraG(),
                c.getAzucarG(),
                c.getSodioMg(),
                porcion);
    }
}
