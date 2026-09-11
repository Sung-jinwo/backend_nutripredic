package com.backend.nutri_predic.alimentacion.dto;

import com.backend.nutri_predic.alimentacion.entity.RegistroAlimento;
import java.math.BigDecimal;

public record RegistroAlimentoResponse(
        Long id,
        Long registroHabitoId,
        Long alimentoId,
        String nombre,
        String categoria,
        BigDecimal cantidad,
        String unidad,
        String momentoComida,
        BigDecimal kcal,
        BigDecimal proteinaG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG) {
    public static RegistroAlimentoResponse from(RegistroAlimento r) {
        var alimento = r.getAlimento();
        return new RegistroAlimentoResponse(
                r.getId(),
                r.getRegistroHabito().getId(),
                alimento == null ? null : alimento.getId(),
                r.getNombreRegistrado(),
                alimento == null ? "PERSONALIZADO" : alimento.getCategoria(),
                r.getCantidad(),
                r.getUnidad().getCodigo(),
                r.getMomentoComida().name(),
                r.getKcalRegistrada(),
                r.getProteinaGRegistrada(),
                r.getCarbohidratosGRegistrados(),
                r.getGrasasGRegistradas());
    }
}
