package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.EquivalenciaUnidadSuplemento;
import java.math.*;
import java.time.*;

public record EquivalenciaUnidadSuplementoResponse(
        Long id,
        Long suplementoId,
        Integer version,
        BigDecimal cantidadOrigen,
        String unidadOrigen,
        BigDecimal cantidadDestino,
        String unidadDestino,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Boolean activo) {
    public static EquivalenciaUnidadSuplementoResponse from(EquivalenciaUnidadSuplemento e) {
        return new EquivalenciaUnidadSuplementoResponse(
                e.getId(),
                e.getSuplemento().getId(),
                e.getVersion(),
                e.getCantidadOrigen(),
                e.getUnidadOrigen().getCodigo(),
                e.getCantidadDestino(),
                e.getUnidadDestino().getCodigo(),
                e.getFechaDesde(),
                e.getFechaHasta(),
                e.getActivo());
    }
}
