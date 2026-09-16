package com.backend.nutri_predic.cliente.peso.dto;

import com.backend.nutri_predic.cliente.peso.entity.RegistroPesoCliente;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadoPesoSemanalResponse(
        Long registroId, LocalDate ultimaFecha, LocalDate proximaFecha,
        boolean habilitado, BigDecimal pesoKg, BigDecimal variacionKg,
        BigDecimal variacionPorcentual, String tendencia, boolean cambioAnomaloConfirmado) {
    public static EstadoPesoSemanalResponse from(RegistroPesoCliente r, LocalDate hoy) {
        if (r == null) return new EstadoPesoSemanalResponse(null, null, hoy, true, null, null, null, "SIN_DATOS", false);
        LocalDate proxima = r.getFechaMedicion().plusDays(7);
        BigDecimal v = r.getVariacionKg();
        String tendencia = v == null || v.abs().compareTo(new BigDecimal("0.10")) < 0 ? "ESTABLE"
                : v.signum() > 0 ? "SUBE" : "BAJA";
        return new EstadoPesoSemanalResponse(r.getId(), r.getFechaMedicion(), proxima,
                !hoy.isBefore(proxima), r.getPesoKg(), v, r.getVariacionPorcentual(),
                tendencia, r.isCambioAnomaloConfirmado());
    }
}
