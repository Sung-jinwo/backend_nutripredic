package com.backend.nutri_predic.alimentacion.dto;

import com.backend.nutri_predic.alimentacion.entity.EquivalenciaUnidadAlimento;
import java.math.*;
import java.time.*;

public record EquivalenciaUnidadAlimentoResponse(
        Long id,
        Integer version,
        BigDecimal cantidadOrigen,
        String unidadOrigen,
        BigDecimal cantidadDestino,
        String unidadDestino,
        String fuenteDatos,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        boolean activo) {
    public static EquivalenciaUnidadAlimentoResponse from(EquivalenciaUnidadAlimento e) {
        return new EquivalenciaUnidadAlimentoResponse(
                e.getId(),
                e.getVersion(),
                e.getCantidadOrigen(),
                e.getUnidadOrigen().getCodigo(),
                e.getCantidadDestino(),
                e.getUnidadDestino().getCodigo(),
                e.getFuenteDatos(),
                e.getFechaDesde(),
                e.getFechaHasta(),
                e.isActivo());
    }
}
