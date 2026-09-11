package com.backend.nutri_predic.conocimiento.practica.dto;

import com.backend.nutri_predic.conocimiento.dto.InstrumentoPublicoResponse;
import com.backend.nutri_predic.conocimiento.practica.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.*;

@Schema(
        description =
                "Sesión PCC-IA pública; no expone respuestas correctas ni explicaciones antes de responder")
public record SesionConocimientoPublicaResponse(
        Long sesionId,
        String estadoAdaptativo,
        java.time.LocalDate fechaEvaluacion,
        String objetivoCliente,
        String clasificacionPredictiva,
        MetaNutricional metaNutricional,
        InstrumentoPublicoResponse instrumento,
        List<PreguntaAdaptativa> preguntasAdaptativas,
        SesionConocimientoResultadoResponse resultadoAdaptativo) {
    public record PreguntaAdaptativa(
            Long id,
            Integer orden,
            String origen,
            String tema,
            String subtema,
            String dificultad,
            String enunciado,
            List<Opcion> opciones) {
        static PreguntaAdaptativa from(PreguntaGeneradaIa p) {
            return new PreguntaAdaptativa(
                    p.getId(),
                    p.getOrden(),
                    "ADAPTATIVA_IA",
                    p.getTema(),
                    p.getSubtema(),
                    p.getDificultad(),
                    p.getEnunciado(),
                    List.of(
                            new Opcion("A", p.getOpcionA()),
                            new Opcion("B", p.getOpcionB()),
                            new Opcion("C", p.getOpcionC()),
                            new Opcion("D", p.getOpcionD())));
        }
    }

    public record Opcion(String codigo, String texto) {}
    public record MetaNutricional(Long planDiarioId, java.time.LocalDate fechaObjetivo,
                                 java.math.BigDecimal kcal, java.math.BigDecimal proteinaG,
                                 java.math.BigDecimal carbohidratosG, java.math.BigDecimal grasasG,
                                 java.math.BigDecimal aguaMl) {}

    public static SesionConocimientoPublicaResponse from(
            SesionConocimientoIa s,
            InstrumentoPublicoResponse i,
            List<PreguntaGeneradaIa> p,
            List<RespuestaAdaptativaIa> respuestas) {
        SesionConocimientoResultadoResponse resultado =
                s.getEstado() == EstadoSesionConocimientoIa.RESPONDIDA
                        ? SesionConocimientoResultadoResponse.from(s, p, respuestas)
                        : null;
        return new SesionConocimientoPublicaResponse(
                s.getId(),
                s.getEstado().name(),
                s.getFechaEvaluacion(),
                s.getObjetivoCliente(),
                s.getClasificacionPredictiva(),
                new MetaNutricional(
                        s.getPlanDiario() == null ? null : s.getPlanDiario().getId(),
                        s.getPlanDiario() == null ? null : s.getPlanDiario().getFechaObjetivo(),
                        s.getMetaKcal(), s.getMetaProteinaG(), s.getMetaCarbohidratosG(),
                        s.getMetaGrasasG(), s.getMetaAguaMl()),
                i,
                p.stream().map(PreguntaAdaptativa::from).toList(),
                resultado);
    }
}
