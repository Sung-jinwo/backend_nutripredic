package com.backend.nutri_predic.suplemento.dto;

import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;

public record SuplementoCatalogoResponse(
        Long id,
        String nombre,
        String tipo,
        String descripcion,
        String beneficios,
        String recomendaciones,
        String marca,
        String presentacion,
        String unidadPresentacion,
        Boolean activo) {
    public static SuplementoCatalogoResponse from(SuplementoCatalogo s) {
        return new SuplementoCatalogoResponse(
                s.getId(),
                s.getNombre(),
                s.getTipo(),
                s.getDescripcion(),
                s.getBeneficios(),
                s.getRecomendaciones(),
                s.getMarca(),
                s.getPresentacion(),
                s.getUnidadPresentacion() == null ? null : s.getUnidadPresentacion().getCodigo(),
                s.getActivo());
    }
}
