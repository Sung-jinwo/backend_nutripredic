package com.backend.nutri_predic.conocimiento.dto;

import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;

public record PreguntaPublicaResponse(
        Long id,
        String enunciado,
        String opcionA,
        String opcionB,
        String opcionC,
        String opcionD,
        String categoria,
        String dificultad) {
    public static PreguntaPublicaResponse from(PreguntaConocimiento p) {
        return new PreguntaPublicaResponse(
                p.getId(),
                p.getEnunciado(),
                p.getOpcionA(),
                p.getOpcionB(),
                p.getOpcionC(),
                p.getOpcionD(),
                p.getCategoria(),
                p.getDificultad());
    }
}
