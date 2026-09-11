package com.backend.nutri_predic.conocimiento.evaluacion.dto;

import com.backend.nutri_predic.conocimiento.evaluacion.entity.*;
import java.time.Instant;
import java.util.List;

public record ResultadoTestResponse(
        Long id,
        Long clienteId,
        Long instrumentoId,
        String momento,
        String estadoValidez,
        int total,
        int correctas,
        double porcentaje,
        String nivel,
        Instant fecha,
        List<ResultadoTemaResponse> resultadosPorTema,
        List<RespuestaHistoricaResponse> respuestas) {
    public record ResultadoTemaResponse(String tema, int correctas, int total, double porcentaje) {
        public static ResultadoTemaResponse from(ResultadoTemaTest r) {
            return new ResultadoTemaResponse(
                    r.getTema(), r.getCorrectas(), r.getTotal(), r.getPorcentaje());
        }
    }

    public record RespuestaHistoricaResponse(
            Long preguntaVersionId,
            String grupoVersion,
            int version,
            String texto,
            String tema,
            String subtema,
            String respuesta,
            String respuestaCorrecta,
            boolean correcta,
            String explicacion,
            String fuente) {
        public static RespuestaHistoricaResponse from(RespuestaTest r) {
            var p = r.getPregunta();
            return new RespuestaHistoricaResponse(
                    p.getId(),
                    p.getGrupoVersion(),
                    p.getVersion(),
                    p.getEnunciado(),
                    p.getTema(),
                    p.getSubtema(),
                    r.getRespuesta(),
                    p.getRespuestaCorrecta(),
                    r.isCorrecta(),
                    p.getExplicacion(),
                    p.getFuenteReferencia());
        }
    }

    public static ResultadoTestResponse from(
            ResultadoTest r, List<ResultadoTemaTest> temas, List<RespuestaTest> respuestas) {
        return new ResultadoTestResponse(
                r.getId(),
                r.getCliente().getId(),
                r.getInstrumento() == null ? null : r.getInstrumento().getId(),
                r.getMomento().name(),
                r.getEstadoValidez().name(),
                r.getTotalPreguntas(),
                r.getCorrectas(),
                r.getPorcentaje(),
                r.getNivel().name(),
                r.getFecha(),
                temas.stream().map(ResultadoTemaResponse::from).toList(),
                respuestas.stream().map(RespuestaHistoricaResponse::from).toList());
    }
}
