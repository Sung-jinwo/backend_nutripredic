package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.SuplementoCliente;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SuplementoClienteResponse(
        Long id,
        Long clienteId,
        Long suplementoId,
        String nombre,
        String marca,
        String tipo,
        String presentacion,
        Double cantidad,
        String unidad,
        String frecuencia,
        String tiempoUso,
        Boolean activo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal cantidadPorToma,
        String unidadCodigo,
        Integer tomasPorPeriodo,
        String periodoFrecuencia,
        boolean frecuenciaEstructuradaCompleta,
        String componentesDeclarados, BigDecimal energiaKcalPorToma, BigDecimal proteinaGPorToma,
        BigDecimal carbohidratosGPorToma, BigDecimal grasasGPorToma, BigDecimal creatinaGPorToma,
        BigDecimal cafeinaMgPorToma, BigDecimal sodioMgPorToma) {
    public static SuplementoClienteResponse from(SuplementoCliente s) {
        boolean completa =
                s.getCantidadPorToma() != null
                        && s.getUnidadMedida() != null
                        && s.getTomasPorPeriodo() != null
                        && s.getPeriodoFrecuencia() != null;
        var c = s.getSuplemento();
        return new SuplementoClienteResponse(
                s.getId(),
                s.getCliente().getId(),
                c.getId(),
                s.getNombreDeclarado(),
                c.getMarca(),
                c.getTipo(),
                c.getPresentacion(),
                s.getCantidad(),
                s.getUnidad(),
                s.getFrecuencia(),
                s.getTiempoUso(),
                s.getActivo(),
                s.getFechaInicio(),
                s.getFechaFin(),
                s.getCantidadPorToma(),
                s.getUnidadMedida() == null ? null : s.getUnidadMedida().getCodigo(),
                s.getTomasPorPeriodo(),
                s.getPeriodoFrecuencia() == null ? null : s.getPeriodoFrecuencia().name(),
                completa, s.getComponentesDeclarados(), s.getEnergiaKcalPorToma(), s.getProteinaGPorToma(),
                s.getCarbohidratosGPorToma(), s.getGrasasGPorToma(), s.getCreatinaGPorToma(),
                s.getCafeinaMgPorToma(), s.getSodioMgPorToma());
    }
}
