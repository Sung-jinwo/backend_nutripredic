package com.backend.nutri_predic.cliente.peso.dto;

import com.backend.nutri_predic.cliente.peso.entity.RegistroPesoCliente;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroPesoResponse(Long id, LocalDate fechaMedicion, BigDecimal pesoKg,
        BigDecimal variacionKg, BigDecimal variacionPorcentual, boolean cambioAnomaloConfirmado) {
    public static RegistroPesoResponse from(RegistroPesoCliente r) {
        return new RegistroPesoResponse(r.getId(), r.getFechaMedicion(), r.getPesoKg(),
                r.getVariacionKg(), r.getVariacionPorcentual(), r.isCambioAnomaloConfirmado());
    }
}
