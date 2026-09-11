package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.ComposicionSuplemento;
import java.math.*;
import java.time.*;

public record ComposicionSuplementoResponse(
        Long id,
        Long suplementoId,
        Integer version,
        BigDecimal cantidadPorcionReferencia,
        String unidadPorcionReferencia,
        BigDecimal energiaKcalPorcion,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Boolean activo,
        String fuenteDatos) {
    public static ComposicionSuplementoResponse from(ComposicionSuplemento c) {
        return new ComposicionSuplementoResponse(
                c.getId(),
                c.getSuplemento().getId(),
                c.getVersion(),
                c.getCantidadPorcionReferencia(),
                c.getUnidadPorcion().getCodigo(),
                c.getEnergiaKcalPorcion(),
                c.getFechaDesde(),
                c.getFechaHasta(),
                c.getActivo(),
                c.getFuenteDatos());
    }
}
