package com.backend.nutri_predic.conocimiento.dto;

import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;
import java.time.Instant;

public record PreguntaAdminResponse(
        Long id,
        String grupoVersion,
        Integer version,
        String texto,
        String enunciado,
        String tema,
        String subtema,
        String dificultad,
        String opcionA,
        String opcionB,
        String opcionC,
        String opcionD,
        String respuestaCorrecta,
        String explicacion,
        String fuenteReferencia,
        String estado,
        Instant creadoEn,
        Instant vigenteDesde) {
    public static PreguntaAdminResponse from(PreguntaConocimiento p) {
        return new PreguntaAdminResponse(
                p.getId(),
                p.getGrupoVersion(),
                p.getVersion(),
                p.getEnunciado(),
                p.getEnunciado(),
                p.getTema(),
                p.getSubtema(),
                p.getDificultad(),
                p.getOpcionA(),
                p.getOpcionB(),
                p.getOpcionC(),
                p.getOpcionD(),
                p.getRespuestaCorrecta(),
                p.getExplicacion(),
                p.getFuenteReferencia(),
                p.getEstadoPregunta().name(),
                p.getCreadoEn(),
                p.getVigenteDesde());
    }
}
