package com.backend.nutri_predic.conocimiento.dto;

import com.backend.nutri_predic.conocimiento.entity.*;
import java.math.BigDecimal;
import java.util.*;

public record InstrumentoPublicoResponse(
        Long id, String codigo, Integer version, String nombre, List<Pregunta> preguntas) {
    public record Pregunta(
            Long id,
            Integer orden,
            BigDecimal puntuacion,
            String enunciado,
            String tema,
            String subtema,
            String dificultad,
            String opcionA,
            String opcionB,
            String opcionC,
            String opcionD) {
        static Pregunta from(InstrumentoPregunta p) {
            var q = p.getPregunta();
            return new Pregunta(
                    q.getId(),
                    p.getOrden(),
                    p.getPuntuacion(),
                    q.getEnunciado(),
                    q.getTema(),
                    q.getSubtema(),
                    q.getDificultad(),
                    q.getOpcionA(),
                    q.getOpcionB(),
                    q.getOpcionC(),
                    q.getOpcionD());
        }
    }

    public static InstrumentoPublicoResponse from(
            InstrumentoConocimiento i, List<InstrumentoPregunta> p) {
        return new InstrumentoPublicoResponse(
                i.getId(),
                i.getCodigo(),
                i.getVersion(),
                i.getNombre(),
                p.stream().map(Pregunta::from).toList());
    }
}
