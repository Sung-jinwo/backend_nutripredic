package com.backend.nutri_predic.conocimiento.practica.dto;

import com.backend.nutri_predic.conocimiento.practica.entity.PreguntaGeneradaIa;
import com.backend.nutri_predic.conocimiento.practica.entity.RespuestaAdaptativaIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Schema(
        description =
                "Resultado educativo adaptativo complementario; no forma parte del cálculo oficial PCC")
public record SesionConocimientoResultadoResponse(
        Long sesionId,
        int totalPreguntas,
        int totalRespondidas,
        int correctas,
        double porcentajeAdaptativo,
        BigDecimal puntajeObtenido,
        BigDecimal puntajeMaximo,
        String nivel,
        @Schema(allowableValues = {"RESPONDIDA"}) String estado,
        Instant respondidaEn,
        List<ResultadoRespuesta> respuestas) {

    public SesionConocimientoResultadoResponse(
            Long sesionId,
            int totalPreguntas,
            int totalRespondidas,
            int correctas,
            double porcentajeAdaptativo,
            String estado,
            Instant respondidaEn,
            List<ResultadoRespuesta> respuestas) {
        this(
                sesionId,
                totalPreguntas,
                totalRespondidas,
                correctas,
                porcentajeAdaptativo,
                BigDecimal.valueOf(correctas * 2L),
                BigDecimal.valueOf(totalPreguntas * 2L),
                correctas == totalPreguntas ? "ALTO" : correctas * 2 >= 7 ? "INTERMEDIO" : "BAJO",
                estado,
                respondidaEn,
                respuestas);
    }

    public record ResultadoRespuesta(
            Long preguntaId,
            String opcionSeleccionada,
            boolean correcta,
            String respuestaCorrecta,
            String explicacion) {}

    public static SesionConocimientoResultadoResponse from(
            SesionConocimientoIa sesion,
            List<PreguntaGeneradaIa> preguntas,
            List<RespuestaAdaptativaIa> respuestas) {
        Map<Long, RespuestaAdaptativaIa> porPregunta =
                respuestas.stream()
                        .collect(
                                Collectors.toMap(
                                        respuesta -> respuesta.getPreguntaGeneradaIa().getId(),
                                        Function.identity()));
        List<ResultadoRespuesta> resultados =
                preguntas.stream()
                        .map(
                                pregunta -> {
                                    RespuestaAdaptativaIa respuesta =
                                            porPregunta.get(pregunta.getId());
                                    return new ResultadoRespuesta(
                                            pregunta.getId(),
                                            respuesta.getOpcionSeleccionada(),
                                            respuesta.isCorrecta(),
                                            pregunta.getRespuestaCorrecta(),
                                            pregunta.getExplicacion());
                                })
                        .toList();
        int correctas =
                (int) respuestas.stream().filter(RespuestaAdaptativaIa::isCorrecta).count();
        return new SesionConocimientoResultadoResponse(
                sesion.getId(),
                preguntas.size(),
                respuestas.size(),
                correctas,
                correctas * 100.0 / preguntas.size(),
                sesion.getPuntajeObtenido(),
                sesion.getPuntajeMaximo(),
                sesion.getNivelResultado(),
                sesion.getEstado().name(),
                sesion.getRespondidaEn(),
                resultados);
    }
}
