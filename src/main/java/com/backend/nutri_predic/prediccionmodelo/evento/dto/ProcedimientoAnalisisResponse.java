package com.backend.nutri_predic.prediccionmodelo.evento.dto;

import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;

public record ProcedimientoAnalisisResponse(
        Long id, String codigo, String nombre, String tipo, boolean activo) {
    public static ProcedimientoAnalisisResponse from(ProcedimientoAnalisis p) {
        return new ProcedimientoAnalisisResponse(
                p.getId(), p.getCodigo(), p.getNombre(), p.getTipo().name(), p.isActivo());
    }
}
