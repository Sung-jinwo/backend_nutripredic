package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.ComponenteComposicionSuplemento;
import java.math.*;

public record ComponenteComposicionSuplementoResponse(
        Long id,
        String tipoComponente,
        String nombreComponente,
        BigDecimal cantidad,
        String unidad) {
    public static ComponenteComposicionSuplementoResponse from(ComponenteComposicionSuplemento c) {
        return new ComponenteComposicionSuplementoResponse(
                c.getId(),
                c.getTipo().name(),
                c.getNombreOtro(),
                c.getCantidad(),
                c.getUnidad().getCodigo());
    }
}
