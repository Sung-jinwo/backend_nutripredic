package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.RegistroConsumoSuplemento;
import java.math.BigDecimal;
import java.time.*;

public record RegistroConsumoSuplementoResponse(
        Long id,
        Long registroHabitoId,
        LocalDate fecha,
        Long suplementoClienteId,
        Long suplementoCatalogoId,
        String nombre,
        String marca,
        String tipo,
        String presentacion,
        BigDecimal cantidadConsumida,
        String unidad,
        Integer numeroTomas,
        String observacion,
        Instant creadoEn) {
    public static RegistroConsumoSuplementoResponse from(RegistroConsumoSuplemento r) {
        var s = r.getSuplementoCliente().getSuplemento();
        return new RegistroConsumoSuplementoResponse(
                r.getId(),
                r.getRegistroHabito().getId(),
                r.getRegistroHabito().getFecha(),
                r.getSuplementoCliente().getId(),
                s.getId(),
                r.getSuplementoCliente().getNombreDeclarado(),
                s.getMarca(),
                s.getTipo(),
                s.getPresentacion(),
                r.getCantidadConsumida(),
                r.getUnidad().getCodigo(),
                r.getNumeroTomas(),
                r.getObservacion(),
                r.getCreadoEn());
    }
}
