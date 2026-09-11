package com.backend.nutri_predic.unidad.dto;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;

public record UnidadMedidaResponse(Long id, String codigo, String nombre) {
    public static UnidadMedidaResponse from(UnidadMedida u) {
        return new UnidadMedidaResponse(u.getId(), u.getCodigo(), u.getNombre());
    }
}
