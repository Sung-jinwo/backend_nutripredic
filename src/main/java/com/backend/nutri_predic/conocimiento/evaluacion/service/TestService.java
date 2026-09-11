package com.backend.nutri_predic.conocimiento.evaluacion.service;

import com.backend.nutri_predic.common.enums.*;
import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoConocimientoRepository;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoPreguntaRepository;
import com.backend.nutri_predic.conocimiento.repository.PreguntaConocimientoRepository;
import com.backend.nutri_predic.conocimiento.evaluacion.dto.*;
import com.backend.nutri_predic.conocimiento.evaluacion.entity.*;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService {
    private final ResultadoTestRepository resultados;
    private final RespuestaTestRepository respuestas;
    private final ResultadoTemaTestRepository resultadosTema;
    private final PreguntaConocimientoRepository preguntas;
    private final InstrumentoConocimientoRepository instrumentos;
    private final InstrumentoPreguntaRepository instrumentoPreguntas;
    private final AccessService access;

    public TestService(
            ResultadoTestRepository r,
            RespuestaTestRepository rr,
            ResultadoTemaTestRepository rt,
            PreguntaConocimientoRepository p,
            AccessService a,
            InstrumentoConocimientoRepository i,
            InstrumentoPreguntaRepository ip) {
        resultados = r;
        respuestas = rr;
        resultadosTema = rt;
        preguntas = p;
        access = a;
        instrumentos = i;
        instrumentoPreguntas = ip;
    }

    @Transactional
    public ResultadoTestResponse submit(ResponderTestRequest request, Authentication auth) {
        var cliente = access.client(request.clienteId(), auth);
        int correctas = 0;
        var instrumento =
                request.instrumentoId() == null
                        ? null
                        : instrumentos
                                .findById(request.instrumentoId())
                                .orElseThrow(() -> new ResourceNotFoundException("Instrumento"));
        var membresias =
                instrumento == null
                        ? List
                                .<com.backend.nutri_predic.conocimiento.entity.InstrumentoPregunta>
                                        of()
                        : instrumentoPreguntas.findByInstrumentoIdOrderByOrdenAsc(
                                instrumento.getId());
        if (instrumento != null
                && (membresias.size() != request.respuestas().size()
                        || !membresias.stream()
                                .map(m -> m.getPregunta().getId())
                                .collect(java.util.stream.Collectors.toSet())
                                .equals(
                                        request.respuestas().stream()
                                                .map(ResponderTestRequest.Respuesta::preguntaId)
                                                .collect(java.util.stream.Collectors.toSet()))))
            throw new BusinessException(
                    "Las respuestas no corresponden exactamente al instrumento");
        var resolved =
                new ArrayList<
                        Map.Entry<
                                com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento,
                                String>>();
        var ids = new HashSet<Long>();
        for (var answer : request.respuestas()) {
            if (!ids.add(answer.preguntaId()))
                throw new BusinessException("No se puede responder dos veces la misma pregunta");
            var p =
                    preguntas
                            .findById(answer.preguntaId())
                            .orElseThrow(() -> new ResourceNotFoundException("Pregunta"));
            if (p.getEstadoPregunta()
                    != com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento
                            .EstadoPregunta.ACTIVA)
                throw new BusinessException("La versión de pregunta ya no está activa");
            String option = answer.opcion().trim().toUpperCase();
            boolean ok = p.getRespuestaCorrecta().equalsIgnoreCase(option);
            if (ok) correctas++;
            resolved.add(Map.entry(p, option));
        }
        double percentage = correctas * 100.0 / request.respuestas().size();
        NivelConocimiento level =
                percentage < 50
                        ? NivelConocimiento.BAJO
                        : percentage < 80 ? NivelConocimiento.MEDIO : NivelConocimiento.ALTO;
        var result =
                new ResultadoTest(
                        cliente, request.respuestas().size(), correctas, percentage, level);
        result.setInstrumento(instrumento);
        result.setMomento(
                request.momento() == null ? MomentoEvaluacion.NO_DETERMINADO : request.momento());
        boolean instrumentoOficial = instrumento != null
                && instrumento.getEstado() == com.backend.nutri_predic.conocimiento.entity.EstadoInstrumento.ACTIVO
                && instrumento.getVigenteDesde() != null
                && !instrumento.getVigenteDesde().isAfter(java.time.Instant.now())
                && instrumento.getFuenteReferencia() != null
                && !instrumento.getFuenteReferencia().isBlank();
        result.setEstadoValidez(instrumentoOficial ? EstadoValidezMedicion.VALIDA : EstadoValidezMedicion.NO_DETERMINADA);
        if (!instrumentoOficial) result.setMotivoInvalidez("INSTRUMENTO_PCC_NO_OFICIAL_O_NO_VIGENTE");
        BigDecimal max =
                membresias.stream()
                        .map(m -> m.getPuntuacion())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setPuntajeMaximo(instrumento == null ? null : max);
        result.setPuntajeObtenido(
                instrumento == null
                        ? null
                        : membresias.stream()
                                .filter(
                                        m ->
                                                resolved.stream()
                                                        .anyMatch(
                                                                e ->
                                                                        e.getKey()
                                                                                        .getId()
                                                                                        .equals(
                                                                                                m.getPregunta()
                                                                                                        .getId())
                                                                                && m.getPregunta()
                                                                                        .getRespuestaCorrecta()
                                                                                        .equalsIgnoreCase(
                                                                                                e
                                                                                                        .getValue())))
                                .map(m -> m.getPuntuacion())
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
        final ResultadoTest resultadoGuardado = resultados.save(result);
        for (var entry : resolved)
            respuestas.save(
                    new RespuestaTest(
                            resultadoGuardado,
                            entry.getKey(),
                            entry.getValue(),
                            entry.getKey()
                                    .getRespuestaCorrecta()
                                    .equalsIgnoreCase(entry.getValue())));
        var porTema = new TreeMap<String, int[]>();
        for (var entry : resolved) {
            String tema = entry.getKey().getTema();
            if (tema == null || tema.isBlank()) tema = "SIN_TEMA";
            var cuenta = porTema.computeIfAbsent(tema, k -> new int[2]);
            cuenta[1]++;
            if (entry.getKey().getRespuestaCorrecta().equalsIgnoreCase(entry.getValue()))
                cuenta[0]++;
        }
        porTema.forEach(
                (tema, cuenta) ->
                        resultadosTema.save(
                                new ResultadoTemaTest(
                                        resultadoGuardado, tema, cuenta[0], cuenta[1])));
        // El nivel oficial vive en ResultadoTest; Cliente ya no duplica niveles manuales
        // (PCC oficial se consulta vía PccIndicatorService / historial de tests).
        return response(resultadoGuardado);
    }

    @Transactional(readOnly = true)
    public List<ResultadoTestResponse> list(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        return resultados.findByClienteIdOrderByFechaDesc(clienteId).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResultadoTestResponse get(Long clienteId, Long id, Authentication auth) {
        access.client(clienteId, auth);
        var r = resultados.findById(id).orElseThrow(() -> new ResourceNotFoundException("Test"));
        if (!r.getCliente().getId().equals(clienteId)) throw new ResourceNotFoundException("Test");
        return response(r);
    }

    private ResultadoTestResponse response(ResultadoTest r) {
        return ResultadoTestResponse.from(
                r,
                resultadosTema.findByResultadoIdOrderByTemaAsc(r.getId()),
                respuestas.findByResultadoIdOrderByIdAsc(r.getId()));
    }
}
